package org.y2k2.globa.application.sqs.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.sqs.dto.common.ConsumerValidateDto;
import org.y2k2.globa.application.sqs.dto.response.ResponseSQSDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.crypto.AESUtil;
import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.domain.section.repository.SectionRepository;
import org.y2k2.globa.domain.summary.repository.SummaryRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.summary.entity.SummaryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SQSService {
    private final AESUtil aesUtil;
    private final FirebaseMessaging firebaseMessaging;

    private final UserRepository userRepository;
    private final FolderShareRepository folderShareRepository;
    private final RecordRepository recordRepository;
    private final SectionRepository sectionRepository;
    private final QuizRepository quizRepository;
    private final AnalysisRepository analysisRepository;
    private final KeywordRepository keywordRepository;
    private final SummaryRepository summaryRepository;
    private final NotificationRepository notificationRepository;

    @Autowired
    private final Bucket bucket;

    @Transactional
    public void success(ResponseSQSDto dto) {
        String encryptedUserId = dto.userId();
        Long decryptedUserId = aesUtil.decrypt(encryptedUserId);
        Long recordId = dto.recordId();
        ConsumerValidateDto validateDto = validateRecord(decryptedUserId, recordId);

        if (!validateDto.isValidated()) {
            if (validateDto.user() != null) {
                sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", validateDto.user());
            }

            return;
        }

        UserEntity user = validateDto.user();
        RecordEntity record = validateDto.record();

        addNotification(user, record);

        sendNotificationShare(record.getTitle() + "이(가) 업로드 되었습니다.", decryptedUserId, record.getFolder().getFolderId());
        sendNotification("업로드 성공", record.getTitle() + "의 업로드 성공하였습니다.", user);
    }

    @Transactional
    public void failed(ResponseSQSDto dto) {
        log.error("Failed to upload and userId = {}, recordId = {}, dto = {}", dto.userId(), dto.recordId(), dto.message());

        String encryptedUserId = dto.userId();
        Long decryptedUserId = aesUtil.decrypt(encryptedUserId);
        Long recordId = dto.recordId();
        ConsumerValidateDto validateDto = validateRecord(decryptedUserId, recordId);

        // 오디오 분석에 실패하였고, 기본 정보도 확인할 수 없다면 로그 남기기
        if (validateDto.user() == null || validateDto.record() == null) {
            return;
        }

        UserEntity user = validateDto.user();
        sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
    }

    private void deleteRecordWithFirebase(String path) {
        try {
            bucket.get(path).delete();
        } catch (Exception e) {
            log.error("Failed to delete audio file and cause: {}", e.getMessage());
        }
    }

    private ConsumerValidateDto validateRecord(Long userId, Long recordId) {
        boolean isValid = true;

        Optional<UserEntity> optionalUser = userRepository.getUserByUserId(userId);
        if (optionalUser.isEmpty()) {
            log.warn("User not found and userId = {}, recordId = {}", userId, recordId);
            return new ConsumerValidateDto(false, null, null);
        }

        Optional<RecordEntity> optionalRecord = recordRepository.getRecord(recordId);
        if (optionalRecord.isEmpty()) {
            log.warn("Record not found and userId = {}, recordId = {}", userId, recordId);
            isValid = false;
        }

        if (optionalRecord.isPresent()) {
            RecordEntity record = optionalRecord.get();

            // 섹션 검증
            List<SectionEntity> sections = sectionRepository.getAllSections(record);
            if (sections.isEmpty()) {
                log.warn("Section not found and userId: {}, recordId: {}", userId, recordId);
                isValid = false;
            }

            // 퀴즈 검증
            List<QuizEntity> quiz = quizRepository.getAllQuizzes(record.getRecordId());
            if (quiz.isEmpty()) {
                log.warn("Quiz not found and userId: {}, recordId: {}", userId, recordId);
                isValid = false;
            }

            // 분석 검증
            List<AnalysisEntity> analysis = analysisRepository.getAllSections(sections.stream().map(SectionEntity::getSectionId).toList());
            if (analysis.isEmpty()) {
                log.warn("Analysis not found and userId: {}, recordId: {}", userId, recordId);
                isValid = false;
            }

            // 키워드 검증
            List<KeywordEntity> keywords = keywordRepository.getAllKeywords(record.getRecordId());
            if (keywords.isEmpty()) {
                log.warn("Keyword not found and userId: {}, recordId: {}", userId, recordId);
                isValid = false;
            }

            // Summary 검증
            List<SummaryEntity> summaries = summaryRepository.getSummaryInSections(sections.stream().map(SectionEntity::getSectionId).toList());
            if (summaries.isEmpty()) {
                log.warn("Summary not found and userId: {}, recordId: {}", userId, recordId);
                isValid = false;
            }

            // 유효성 검사 실패하면 퀴즈, 키워드, 분석 데이터 등 삭제
            if (!isValid) {
                quizRepository.deleteAll(quiz);
                analysisRepository.deleteAll(analysis);
                sectionRepository.deleteAll(sections);
                keywordRepository.deleteAll(keywords);
                summaryRepository.deleteAll(summaries);

                deleteRecordWithFirebase(record.getPath());
                recordRepository.delete(record);

                sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", optionalUser.get());
                return new ConsumerValidateDto(false, optionalUser.get(), record);

            }

            return new ConsumerValidateDto(true, optionalUser.get(), record);
        }

        // Record가 없으면 유효하지 않음
        sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", optionalUser.get());
        return new ConsumerValidateDto(false, optionalUser.get(), null);
    }

    private void addNotification(UserEntity user, RecordEntity record) {
        NotificationEntity entity = new NotificationEntity();
        entity.setType(NotificationType.UPLOAD_SUCCESS);
        entity.setReceiver(user);
        entity.setSender(user);
        entity.setFolder(record.getFolder());
        entity.setRecord(record);

        notificationRepository.save(entity);
    }

    private void sendNotification(String title, String body, UserEntity user) {
        if (!user.getUploadNofi() || user.getNotificationToken() == null) return;

        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(title)
                            .setBody(body)
                            .build())
                    .setToken(user.getNotificationToken())
                    .build();

            firebaseMessaging.send(message, false);
        }  catch (Exception e) {
            log.error("Failed to send upload message and cause: {}", e.getMessage());
        }
    }

    private void sendNotificationShare(String body, Long fromId, Long folderId) {
        List<FolderShareEntity> targetFolderShares = folderShareRepository.getAllShareInvitationsWithoutMe(folderId, fromId);
        List<Message> messages = new ArrayList<>();

        try {
            for (FolderShareEntity targetFolderShare : targetFolderShares) {
                boolean isNotTarget = !targetFolderShare.getTargetUser().getShareNofi()
                        || targetFolderShare.getTargetUser().getNotificationToken() == null;
                if (isNotTarget) {
                    continue;
                }

                Message message = Message.builder()
                        .setToken(targetFolderShare.getTargetUser().getNotificationToken())
                        .setNotification(Notification.builder()
                                .setTitle("새로운 업로드")
                                .setBody(body)
                                .build())
                        .build();
                messages.add(message);
            }

            firebaseMessaging.sendEach(messages, false);
        }  catch (Exception e) {
            log.debug("Failed to send share upload notification : {}", e.getMessage());
        }
    }
}

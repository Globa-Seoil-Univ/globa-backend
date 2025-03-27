package org.y2k2.globa.application.kafka.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.domain.analysis.repository.AnalysisRepository;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.repository.NotificationJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.section.repository.SectionJpaRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.dto.response.kafka.ConsumerValidateDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
import org.y2k2.globa.insfrastructure.persistence.jpa.repository.UserJpaRepository;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.dto.response.kafka.ResponseKafkaDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {
    private final FirebaseMessaging firebaseMessaging;
    private final UserJpaRepository userJpaRepository;
    private final FolderShareJpaRepository folderShareJpaRepository;
    private final RecordJpaRepository recordJpaRepository;
    private final SectionJpaRepository sectionJpaRepository;
    private final QuizJpaRepository quizJpaRepository;
    private final AnalysisRepository analysisRepository;
    private final KeywordJpaRepository keywordJpaRepository;
    private final NotificationJpaRepository notificationJpaRepository;

    @Autowired
    private final Bucket bucket;

    @Transactional
    public void success(ResponseKafkaDto dto) {
        long userId = dto.getUserId();
        long recordId = dto.getRecordId();
        ConsumerValidateDto validateDto = validateRecord(userId, recordId);

        if (!validateDto.getIsValidated()) {
            if (validateDto.getUser() != null || validateDto.getRecord() != null) {
                sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", validateDto.getUser());
            }

            return;
        }

        UserEntity user = validateDto.getUser();
        RecordEntity record = validateDto.getRecord();

        addNotification(user, record, NotificationType.UPLOAD_SUCCESS.getTypeId());

        sendNotificationShare(record.getTitle() + "이(가) 업로드 되었습니다.", userId, record.getFolder().getFolderId());
        sendNotification("업로드 성공", record.getTitle() + "의 업로드 성공하였습니다.", user);
    }

    @Transactional
    public void failed(ResponseKafkaDto dto) {
        log.error("Failed to upload and userId: {}, recordId: {}, dto: {}", dto.getUserId(), dto.getRecordId(), dto.getMessage());

        long userId = dto.getUserId();
        long recordId = dto.getRecordId();
        ConsumerValidateDto validateDto = validateRecord(userId, recordId);

        // 오디오 분석에 실패하였고, 기본 정보도 확인할 수 없다면 로그 남기기
        if (validateDto.getUser() == null || validateDto.getRecord() == null) {
            log.warn("User not found and userId: {}, recordId: {}", userId, recordId);
            return;
        }

        UserEntity user = validateDto.getUser();
        RecordEntity record = validateDto.getRecord();

        // if => 오디오 분석엔 성공했지만, 알 수 없는 이유로 메시지 잘못 보냄 또는 에러가 발생 했을 때
        // else => 오디오 분석엔 실패했고, 기본 정보가 있다면 업로드 실패 알림 보내기 (일반적인 상황)
        if (validateDto.getIsValidated()) {
            addNotification(user, record, NotificationType.UPLOAD_SUCCESS.getTypeId());

            sendNotificationShare(record.getTitle() + "이(가) 업로드 되었습니다.", userId, record.getFolder().getFolderId());
            sendNotification("업로드 성공", record.getTitle() + "의 업로드 성공하였습니다.", user);
        } else {
            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
        }
    }

    private void deleteRecordWithFirebase(String path) {
        try {
            bucket.get(path).delete();
        } catch (Exception e) {
            log.error("Failed to delete audio file and cause: {}", e.getMessage());
        }
    }

    private ConsumerValidateDto validateRecord(long userId, long recordId) {
        // TODO : Summary 검증 추가
        boolean isValid = true;

        UserEntity user = userJpaRepository.findByUserId(userId)
                .orElseThrow(() -> {
                    log.warn("User not found and userId: {}, recordId: {}", userId, recordId);
                    return new CustomException(ErrorCode.NOT_FOUND_USER);
                });

        Optional<RecordEntity> record = recordJpaRepository.findByRecordId(recordId);
        if (record.isEmpty()) {
            log.warn("Record not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<SectionEntity> sections = sectionJpaRepository.existsByRecord(record);
        if (sections) {
            log.warn("Section not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<QuizEntity> quiz = quizJpaRepository.findAllByRecord(record);
        if (quiz.isEmpty()) {
            log.warn("Quiz not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<AnalysisEntity> analysis = new ArrayList<>();

        for (SectionEntity section : sections) {
            analysis.addAll(analysisRepository.findAllBySection(section));
        }

        if (analysis.isEmpty()) {
            log.warn("Analysis not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<KeywordEntity> keywords = keywordJpaRepository.findAllByRecord(record);
        if (keywords.isEmpty()) {
            log.warn("Keyword not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        // 유효성 검사 실패하면 퀴즈, 키워드, 분석 데이터 등 삭제
        if (!isValid) {
            quizJpaRepository.deleteAllInBatch(quiz);
            analysisRepository.deleteAllInBatch(analysis);
            sectionJpaRepository.deleteAllInBatch(sections);
            keywordJpaRepository.deleteAllInBatch(keywords);
            if (record.isPresent()) {
                deleteRecordWithFirebase(record.get().getPath());
                recordJpaRepository.delete(record.get());
            }

            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return new ConsumerValidateDto(false, user, record);
        }

        return new ConsumerValidateDto(true, user, record);
    }

    private void addNotification(UserEntity user, RecordEntity record, char typeId) {
        NotificationEntity entity = new NotificationEntity();
        entity.setTypeId(typeId);
        entity.setReceiver(user);
        entity.setSender(user);
        entity.setFolder(record.getFolder());
        entity.setRecord(record);

        notificationJpaRepository.save(entity);
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

    private void sendNotificationShare(String body, long fromId, long folderId) {
        List<FolderShareEntity> targetFolderShares = folderShareJpaRepository.findAllByFolderFolderId(folderId);
        List<Message> messages = new ArrayList<>();

        try {
            for (FolderShareEntity targetFolderShare : targetFolderShares) {
                boolean isNotTarget = !targetFolderShare.getTargetUser().getShareNofi()
                        || targetFolderShare.getTargetUser().getNotificationToken() == null
                        || targetFolderShare.getTargetUser().getUserId().equals(fromId);
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
            log.debug("Failed to send share upload notification : " + e.getMessage());
        }
    }
}

package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.ConsumerValidateDto;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.dto.ResponseKafkaDto;
import org.y2k2.globa.entity.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaService {
    private final FirebaseMessaging firebaseMessaging;
    private final UserRepository userRepository;
    private final FolderShareRepository folderShareRepository;
    private final RecordRepository recordRepository;
    private final SectionRepository sectionRepository;
    private final QuizRepository quizRepository;
    private final AnalysisRepository analysisRepository;
    private final KeywordRepository keywordRepository;
    private final NotificationRepository notificationRepository;

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
        boolean isValid = true;

        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            log.warn("User not found and userId: {}, recordId: {}", userId, recordId);
            return new ConsumerValidateDto(false, null, null);
        }

        RecordEntity record = recordRepository.findByRecordId(recordId);
        if (record == null) {
            log.warn("Record not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<SectionEntity> sections = sectionRepository.findAllByRecord(record);
        if (sections.isEmpty()) {
            log.warn("Section not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        List<QuizEntity> quiz = quizRepository.findAllByRecord(record);
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

        List<KeywordEntity> keywords = keywordRepository.findAllByRecord(record);
        if (keywords.isEmpty()) {
            log.warn("Keyword not found and userId: {}, recordId: {}", userId, recordId);
            isValid = false;
        }

        // 유효성 검사 실패하면 퀴즈, 키워드, 분석 데이터 등 삭제
        if (!isValid) {
            quizRepository.deleteAll(quiz);
            analysisRepository.deleteAll(analysis);
            sectionRepository.deleteAll(sections);
            keywordRepository.deleteAll(keywords);
            if (record != null) {
                deleteRecordWithFirebase(record.getPath());
                recordRepository.delete(record);
            }

            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return new ConsumerValidateDto(false, user, record);
        }

        return new ConsumerValidateDto(true, user, record);
    }

    private void addNotification(UserEntity user, RecordEntity record, char typeId) {
        NotificationEntity entity = new NotificationEntity();
        entity.setTypeId(typeId);
        entity.setToUser(user);
        entity.setFromUser(user);
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

    private void sendNotificationShare(String body, long fromId, long folderId) {
        List<FolderShareEntity> targetFolderShares = folderShareRepository.findAllByFolderFolderId(folderId);
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

package org.y2k2.globa.service;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.KafkaResponseDto;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.SectionEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.repository.*;

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

    @Transactional
    public void success(KafkaResponseDto dto) {
        long userId = dto.getUserId();
        long recordId = dto.getRecordId();

        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            log.warn("User not found and userId: {}, recordId: {}", userId, recordId);
            return;
        }

        RecordEntity record = recordRepository.findByRecordId(recordId);
        if (record == null) {
            log.warn("Record not found and userId: {}, recordId: {}", userId, recordId);
            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return;
        }

        SectionEntity section = sectionRepository.findFirstByRecord(record);
        if (section == null) {
            log.warn("Section not found and userId: {}, recordId: {}", userId, recordId);
            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return;
        }

        boolean isExistedQuiz = quizRepository.existsByRecord(record);
        if (!isExistedQuiz) {
            log.warn("Quiz not found and userId: {}, recordId: {}", userId, recordId);
            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return;
        }

        boolean existedAnalysis = analysisRepository.existsBySection(section);
        if (!existedAnalysis) {
            log.warn("Analysis not found and userId: {}, recordId: {}", userId, recordId);
            sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
            return;
        }
        
        // 유효성 검사 실패하면 퀴즈, 키워드, 분석 데이터 등 삭제

        // 공유된 폴더라면 공유된 사용자에게도 알림을 보냅니다. -> 아직 구현 안 됨
        sendNotificationShare(record.getTitle() + "이(가) 업로드 되었습니다.", userId, record.getFolder().getFolderId());
        sendNotification("업로드 성공", record.getTitle() + "의 업로드 성공하였습니다.", user);
    }

    public void failed(KafkaResponseDto dto) {
        log.error("Failed to upload and userId: {}, recordId: {}, dto: {}", dto.getUserId(), dto.getRecordId(), dto.getMessage());

        long userId = dto.getUserId();
        long recordId = dto.getRecordId();

        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            log.warn("User not found and userId: {}, recordId: {}", userId, recordId);
            return;
        }

        sendNotification("업로드 실패", "업로드 실패하였습니다.\n나중에 다시 시도해주세요.", user);
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

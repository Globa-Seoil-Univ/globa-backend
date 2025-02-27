package org.y2k2.globa.util.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.common.fcm.FcmData;
import org.y2k2.globa.dto.common.notification.*;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.type.NotificationType;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUtil {
    @Value("${fcm.dry-run:true}")
    private Boolean dryRun;

    private final FirebaseMessaging firebaseMessaging;

    private final UserRepository userRepository;

    private boolean deniedFcm(NotificationType type, UserEntity receiver) {
        if (receiver.getNotificationToken() == null || receiver.getNotificationToken().isEmpty()) {
            log.warn("Notification token is null. userId = {}, name = {}", receiver.getUserId(), receiver.getName());
            return true;
        }

        return switch (type) {
            case INQUIRY -> receiver.getPrimaryNofi() == null || !receiver.getPrimaryNofi();
            case UPLOAD_SUCCESS, UPLOAD_FAILED -> receiver.getUploadNofi() == null || !receiver.getUploadNofi();
            case SHARE_FOLDER_INVITE, SHARE_FOLDER_ADD_FILE, SHARE_FOLDER_ADD_USER, SHARE_FOLDER_ADD_COMMENT ->
                    receiver.getShareNofi() == null || !receiver.getShareNofi();
            default -> true;
        };
    }

    private FcmData extractFcmData(SendMessage sendMessage) {
        if (sendMessage instanceof RequestNotificationWithFolderShareAddUserDto) {
            return FcmData.builder()
                    .folderId(((RequestNotificationWithFolderShareAddUserDto) sendMessage).getFolder().getFolderId())
                    .build();
        } else if (sendMessage instanceof RequestNotificationWithFolderShareCommentDto) {
            return FcmData.builder()
                    .folderId(((RequestNotificationWithFolderShareCommentDto) sendMessage).getFolder().getFolderId())
                    .recordId(((RequestNotificationWithFolderShareCommentDto) sendMessage).getRecord().getRecordId())
                    .build();
        } else if (sendMessage instanceof RequestNotificationWithInquiryDto) {
            return FcmData.builder()
                    .inquiryId(((RequestNotificationWithInquiryDto) sendMessage).getInquiry().getInquiryId())
                    .build();
        } else {
            return FcmData.builder().build();
        }
    }

    public void sendFcmMessage(SendMessage sendMessage) {
        if (deniedFcm(sendMessage.getNotificationType(), sendMessage.getReceiver())) {
            return;
        }

        try {
            FcmData data = extractFcmData(sendMessage);

            Message message = Message.builder()
                    .setToken(sendMessage.getReceiver().getNotificationToken())
                    .putData("recordId", data.recordId().toString())
                    .putData("folderId", data.folderId().toString())
                    .putData("inquiryId", data.inquiryId().toString())
                    .putData("notificationType", sendMessage.getNotificationType().toStringType())
                    .setNotification(Notification.builder()
                            .setTitle(sendMessage.getTitle())
                            .setBody(sendMessage.getBody())
                            .build())
                    .build();

            firebaseMessaging.send(message, dryRun);
        } catch (FirebaseMessagingException e) {
            if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT || e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                sendMessage.getReceiver().setNotificationToken(null);
                sendMessage.getReceiver().setNotificationTokenTime(null);
                userRepository.save(sendMessage.getReceiver());
                log.debug("Delete Notification Token : " + sendMessage.getReceiver().getUserId());
            }

            log.error("Failed to send FCM message = {}, Trace = {} ", sendMessage.getReceiver().getUserId(), e.getMessage());
        } catch (Exception e) {
            log.error("Failed to send FCM message = {}, Trace = {} ", sendMessage.getReceiver().getUserId(), e.getMessage());
        }
    }

    public void sendFcmMessages(List<? extends SendMessage> sendMessage) {
        if (sendMessage.isEmpty()) {
            return;
        }

        List<? extends SendMessage> accessTarget = sendMessage.stream()
                .filter(s -> !deniedFcm(s.getNotificationType(), s.getReceiver()))
                .toList();

        FcmData data = extractFcmData(accessTarget.get(0));
        String title = accessTarget.get(0).getTitle();
        String body = accessTarget.get(0).getBody();
        String type = accessTarget.get(0).getNotificationType().toStringType();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(
                        accessTarget.stream().map(
                                s -> s.getReceiver().getNotificationToken()
                        ).toList()
                )
                .putData("recordId", data.recordId().toString())
                .putData("folderId", data.folderId().toString())
                .putData("inquiryId", data.inquiryId().toString())
                .putData("notificationType", type)
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message, dryRun);

            for (int i = 0; i < response.getResponses().size(); i++) {
                if (response.getResponses().get(i).isSuccessful()) {
                    log.info("Successfully sent message = " + response.getResponses().get(i).getMessageId());
                } else {
                    if (response.getResponses().get(i).getException() != null) {
                        FirebaseMessagingException e = response.getResponses().get(i).getException();

                        if (e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT || e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED) {
                            accessTarget.get(i).getReceiver().setNotificationToken(null);
                            accessTarget.get(i).getReceiver().setNotificationTokenTime(null);
                            userRepository.save(accessTarget.get(i).getReceiver());
                            log.debug("Delete Notification Token : " + accessTarget.get(i).getReceiver().getUserId());
                        }
                    }

                    log.error("Failed to send message = " + response.getResponses().get(i).getException());
                }
            }
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM Trace = {} ", (Object) e.getStackTrace());
        }
    }

    public void sendFcmMessageToTopic(RequestNotificationWithTopicDto dto) {
        try {
            Message message = Message.builder()
                    .setTopic(dto.getTopic())
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .build();

            firebaseMessaging.send(message, dryRun);
        } catch (FirebaseMessagingException e) {
            log.error("Failed to send FCM message = {}, Trace = {} ", dto.getTopic(), e.getMessage());
        }
    }

    public void subscribeTopic(String token, String topic) {
        if (token == null || token.isEmpty()) {
            log.warn("Notification token is null. userId = {}", token);
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.subscribeToTopic(List.of(token), topic);
            log.info("Successfully subscribed to topic: " + response.getSuccessCount());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to subscribe to topic: " + e.getMessage());
        }
    }

    public void unsubscribeTopic(String token, String topic) {
        if (token == null || token.isEmpty()) {
            log.warn("Notification token is null. userId = {}", token);
            return;
        }

        try {
            TopicManagementResponse response = firebaseMessaging.unsubscribeFromTopic(List.of(token), topic);
            log.info("Successfully unsubscribed from topic: " + response.getSuccessCount());
        } catch (FirebaseMessagingException e) {
            log.error("Failed to unsubscribe from topic: " + e.getMessage());
        }
    }
}

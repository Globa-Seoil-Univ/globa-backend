package org.y2k2.globa.util.fcm;

import com.google.firebase.messaging.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.common.fcm.SendMessage;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.type.NotificationType;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class MessageUtil {
    private final FirebaseMessaging firebaseMessaging;

    private final UserRepository userRepository;

    private boolean deniedFcm(NotificationType type, UserEntity receiver) {
        if (receiver.getNotificationToken() == null
                || receiver.getNotificationToken().isEmpty()) {
            log.warn("Notification token is null. userId = {}, name = {}", receiver.getUserId(), receiver.getName());
            return true;
        }

        return switch (type) {
            case NOTICE, INQUIRY -> receiver.getPrimaryNofi() == null || !receiver.getPrimaryNofi();
            case UPLOAD_SUCCESS, UPLOAD_FAILED -> receiver.getUploadNofi() == null || !receiver.getUploadNofi();
            case SHARE_FOLDER_INVITE, SHARE_FOLDER_ADD_FILE, SHARE_FOLDER_ADD_USER, SHARE_FOLDER_ADD_COMMENT ->
                    receiver.getShareNofi() == null || !receiver.getShareNofi();
            default -> true;
        };
    }

    public void sendFcmMessage(SendMessage sendMessage) {
        if (deniedFcm(sendMessage.getNotificationType(), sendMessage.getReceiver())) {
            return;
        }

        try {
            Message message = Message.builder()
                    .setToken(sendMessage.getReceiver().getNotificationToken())
                    .setNotification(Notification.builder()
                            .setTitle(sendMessage.getTitle())
                            .setBody(sendMessage.getBody())
                            .build())
                    .build();

            firebaseMessaging.send(message);
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

        String title = accessTarget.get(0).getTitle();
        String body = accessTarget.get(0).getBody();

        MulticastMessage message = MulticastMessage.builder()
                .addAllTokens(
                        accessTarget.stream().map(
                                s -> s.getReceiver().getNotificationToken()
                        ).toList()
                )
                .setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build())
                .build();

        try {
            BatchResponse response = firebaseMessaging.sendEachForMulticast(message);

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
}

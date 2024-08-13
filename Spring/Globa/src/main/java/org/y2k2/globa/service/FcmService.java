package org.y2k2.globa.service;

import com.google.firebase.messaging.*;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.FcmException;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {
    private final FirebaseMessaging firebaseMessaging;

    @Transactional
    public void sendTopicNotification(RequestFcmTopicDto dto) {
        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .setTopic(dto.getTopic())
                    .build();

            firebaseMessaging.send(message, false);
        }  catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_FCM_SEND);
        }
    }
}

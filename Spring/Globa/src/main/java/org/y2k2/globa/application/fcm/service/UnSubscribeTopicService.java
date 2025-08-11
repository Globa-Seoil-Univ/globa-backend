package org.y2k2.globa.application.fcm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.fcm.dto.common.FcmSubscribeEvent;
import org.y2k2.globa.application.fcm.dto.common.FcmUnSubscribeEvent;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class UnSubscribeTopicService {
    private final ApplicationEventPublisher publisher;

    private final FindUserUseCase findUserUseCase;

    public void unsubscribe(RequestSubscribeTopicDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);

        if (user.getNotificationToken() == null || user.getNotificationToken().isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);
        }

        publisher.publishEvent(
                FcmUnSubscribeEvent.builder()
                        .topic(dto.topic())
                        .token(user.getNotificationToken())
                        .build()
        );
    }
}

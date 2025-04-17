package org.y2k2.globa.application.fcm.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;

@Service
@RequiredArgsConstructor
public class PushMessageService {
    private final ApplicationEventPublisher publisher;

    private final VerifyUserWritableUseCase verifyUserWritableUseCase;

    public void send(RequestFcmTopicDto dto, Long userId) {
        verifyUserWritableUseCase.execute(userId);

        RequestNotificationWithTopicDto info = RequestNotificationWithTopicDto.builder()
                .title(dto.title())
                .body(dto.body())
                .topic(dto.topic())
                .build();

        publisher.publishEvent(info);
    }
}

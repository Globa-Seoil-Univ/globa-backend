package org.y2k2.globa.application.fcm.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;

@ExtendWith(MockitoExtension.class)
public class PushMessageServiceTest {
    @InjectMocks
    private PushMessageService pushMessageService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private VerifyUserWritableUseCase verifyUserWritableUseCase;

    @Test
    @DisplayName("Fcm 전송 - 성공")
    void send() {
        RequestFcmTopicDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        Mockito
                .doNothing()
                .when(verifyUserWritableUseCase)
                .execute(Mockito.anyLong());

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));

        pushMessageService.send(dto, 1L);

        Mockito
                .verify(verifyUserWritableUseCase, Mockito.times(1))
                .execute(Mockito.anyLong());

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(RequestNotificationWithTopicDto.class));
    }
}

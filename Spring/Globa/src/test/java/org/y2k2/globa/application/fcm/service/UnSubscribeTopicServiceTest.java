package org.y2k2.globa.application.fcm.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.fcm.dto.common.FcmUnSubscribeEvent;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class UnSubscribeTopicServiceTest {
    @InjectMocks
    private UnSubscribeTopicService unSubscribeTopicService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private FindUserUseCase findUserUseCase;

    @Test
    @DisplayName("FCM 구독 해제 - 성공")
    void unsubscribe_Success() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("notificationToken", "sampleToken")
                .set("notificationTokenTime", new CustomTimestamp().getTimestamp())
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(FcmUnSubscribeEvent.class));

        unSubscribeTopicService.unsubscribe(dto, userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(FcmUnSubscribeEvent.class));
    }

    @Test
    @DisplayName("FCM 구독 해제 - 실패 (알림 토큰 X)")
    void unsubscribe_Fail_NoNotificationToken() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("notificationToken", null) // 알림 토큰이 없음
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Assertions
                .assertThatThrownBy(() -> unSubscribeTopicService.unsubscribe(dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(FcmUnSubscribeEvent.class));
    }
}
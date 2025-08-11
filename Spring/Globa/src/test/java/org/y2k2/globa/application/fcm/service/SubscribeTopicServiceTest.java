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
import org.y2k2.globa.application.fcm.dto.common.FcmSubscribeEvent;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class SubscribeTopicServiceTest {
    @InjectMocks
    private SubscribeTopicService subscribeTopicService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("토픽 구독 - 성공 (공지, 알림 O)")
    void subscribeNotice_Success() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.NOTICE.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("primaryNofi", true)
                .set("notificationToken", "sampleToken")
                .set("notificationTokenTime", new CustomTimestamp().getTimestamp())
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));

        subscribeTopicService.subscribe(dto, userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.never())
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }

    @Test
    @DisplayName("토픽 구독 - 성공 (공지, 알림 X)")
    void subscribeWithoutNotification_Success() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.NOTICE.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("primaryNofi", false)
                .set("notificationToken", "sampleToken")
                .set("notificationTokenTime", new CustomTimestamp().getTimestamp())
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito
                .when(userRepository.save(Mockito.any(UserEntity.class)))
                .thenReturn(user);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));

        subscribeTopicService.subscribe(dto, userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }

    @Test
    @DisplayName("토픽 구독 - 성공 (이벤트, 알림 O)")
    void subscribeEvent_Success() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.EVENT.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("eventNofi", true)
                .set("notificationToken", "sampleToken")
                .set("notificationTokenTime", new CustomTimestamp().getTimestamp())
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));

        subscribeTopicService.subscribe(dto, userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.never())
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }

    @Test
    @DisplayName("토픽 구독 - 성공 (이벤트, 알림 X)")
    void subscribeEventWithoutNotification_Success() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.EVENT.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("eventNofi", false)
                .set("notificationToken", "sampleToken")
                .set("notificationTokenTime", new CustomTimestamp().getTimestamp())
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito
                .when(userRepository.save(Mockito.any(UserEntity.class)))
                .thenReturn(user);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));

        subscribeTopicService.subscribe(dto, userId);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.times(1))
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }

    @Test
    @DisplayName("토픽 구독 - 실패 (공지, 알림 토큰 X)")
    void subscribeNotice_Fail_NoNotificationToken() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.NOTICE.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("primaryNofi", true)
                .set("notificationToken", null)
                .set("notificationTokenTime", null)
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Assertions
                .assertThatThrownBy(() -> subscribeTopicService.subscribe(dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.never())
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }

    @Test
    @DisplayName("토픽 구독 - 실패 (이벤트, 알림 토큰 X)")
    void subscribeEvent_Fail_NoNotificationToken() {
        Long userId = 1L;
        RequestSubscribeTopicDto dto = new RequestSubscribeTopicDto(FcmTopic.EVENT.getTopic());

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("eventNofi", true)
                .set("notificationToken", null)
                .set("notificationTokenTime", null)
                .sample();

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Assertions
                .assertThatThrownBy(() -> subscribeTopicService.subscribe(dto, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(userRepository, Mockito.never())
                .save(Mockito.any(UserEntity.class));

        Mockito
                .verify(publisher, Mockito.never())
                .publishEvent(Mockito.any(FcmSubscribeEvent.class));
    }
}

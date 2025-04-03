package org.y2k2.globa.service.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.service.GetUserNotificationService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetUserNotificationServiceTest {
    @InjectMocks
    private GetUserNotificationService getUserNotificationService;

    @Mock
    private FindUserUseCase findUserUseCase;

    @Test
    @DisplayName("유저 알림 설정 조회 - 성공")
    void getUserNotification() {
        Long userId = 1L;

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("eventNofi", true)
                .set("shareNofi", true)
                .set("uploadNofi", true)
                .set("isDeleted", false)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        ResponseNotificationSettingDto response = getUserNotificationService.getUserNotification(userId);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.eventNofi()).isTrue();
        Assertions.assertThat(response.shareNofi()).isTrue();
        Assertions.assertThat(response.uploadNofi()).isTrue();
    }

    @Test
    @DisplayName("유저 알림 설정 조회 - 실패 (유저 없음)")
    void getUserNotificationFailNotFound() {
        Long userId = 1L;

        Mockito.when(findUserUseCase.execute(userId))
                        .thenThrow(new CustomException(ErrorCode.NOT_FOUND_USER));

        Assertions.assertThatThrownBy(() -> getUserNotificationService.getUserNotification(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_USER);
    }

    @Test
    @DisplayName("유저 알림 설정 조회 - 실패 (삭제된 유저)")
    void getUserNotificationFailDeleted() {
        Long userId = 1L;

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", true)
                .sample();

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Assertions.assertThatThrownBy(() -> getUserNotificationService.getUserNotification(userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DELETED_USER);
    }
}

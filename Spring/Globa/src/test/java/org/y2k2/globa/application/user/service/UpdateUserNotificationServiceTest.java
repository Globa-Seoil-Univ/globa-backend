package org.y2k2.globa.application.user.service;


import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestNotificationSettingDto;
import org.y2k2.globa.application.user.service.UpdateUserNotificationService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateUserNotificationServiceTest {
    @InjectMocks
    private UpdateUserNotificationService updateUserNotificationService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UpdateUserUseCase updateUserUseCase;

    @Test
    @DisplayName("알림 수정 - 성공")
    void updateNotificationSettings_Success() {
        Long userId = 1L;
        RequestNotificationSettingDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestNotificationSettingDto.class);

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(FixtureMonkey.builder()
                        .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                        .plugin(new JakartaValidationPlugin())
                        .build()
                        .giveMeOne(UserEntity.class));

        Mockito.doNothing()
                .when(updateUserUseCase)
                .execute(ArgumentMatchers.any(UpdateUserCommand.class));

        updateUserNotificationService.update(dto, userId);
    }
}

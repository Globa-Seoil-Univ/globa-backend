package org.y2k2.globa.application.user.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.user.service.UpsertFcmService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpsertFcmServiceTest {
    @InjectMocks
    private UpsertFcmService upsertFcmService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("FCM 토큰 업데이트 - 성공")
    void upsertToken() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", false)
                .sample();
        RequestNotificationTokenDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(RequestNotificationTokenDto.class);

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(userRepository.save(user))
                .thenReturn(user);

        upsertFcmService.upsert(dto, userId);
    }
}

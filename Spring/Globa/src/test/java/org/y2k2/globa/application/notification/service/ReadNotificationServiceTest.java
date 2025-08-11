package org.y2k2.globa.application.notification.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.notification.command.VerifyModifyNotificationCommand;
import org.y2k2.globa.application.notification.usecase.VerifyModifyNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.domain.notificationread.repository.NotificationReadRepository;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class ReadNotificationServiceTest {
    @InjectMocks
    private ReadNotificationService readNotificationService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyModifyNotificationUseCase verifyModifyNotificationUseCase;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationReadRepository notificationReadRepository;

    @Test
    @DisplayName("알림 읽기 - 성공")
    public void read_Success() {
        Long notificationId = 1L,
                userId = 1L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("notificationId", notificationId)
                .set("receiver.userId", userId)
                .sample();

        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .sample();

        Mockito
                .when(notificationReadRepository.getNotificationRead(notificationId))
                .thenReturn(Optional.empty());

        Mockito
                .when(notificationRepository.getNotification(notificationId))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(userId))
                .thenReturn(user);

        readNotificationService.read(notificationId, userId);

        Mockito
                .verify(notificationReadRepository, Mockito.times(1))
                .getNotificationRead(notificationId);

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotification(notificationId);

        Mockito
                .verify(verifyModifyNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .verify(findUserUseCase, Mockito.times(1))
                .execute(userId);

        Mockito
                .verify(notificationReadRepository, Mockito.times(1))
                .save(Mockito.any(NotificationReadEntity.class));
    }

    @Test
    @DisplayName("알림 읽기 - 성공 (이미 읽은 알림)")
    public void read_AlreadyReadNotification() {
        Long notificationId = 1L;

        Mockito
                .when(notificationReadRepository.getNotificationRead(notificationId))
                .thenReturn(Optional.of(new NotificationReadEntity()));

        readNotificationService.read(notificationId, 1L);

        Mockito
                .verify(notificationReadRepository, Mockito.times(1))
                .getNotificationRead(notificationId);
        Mockito
                .verify(notificationRepository, Mockito.never())
                .getNotification(notificationId);
        Mockito
                .verify(verifyModifyNotificationUseCase, Mockito.never())
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));
        Mockito
                .verify(findUserUseCase, Mockito.never())
                .execute(Mockito.anyLong());
        Mockito
                .verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }
}

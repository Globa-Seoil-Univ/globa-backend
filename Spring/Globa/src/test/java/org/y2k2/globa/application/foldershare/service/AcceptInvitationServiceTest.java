package org.y2k2.globa.application.foldershare.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyInvitationUseCase;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithFolderShareDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class AcceptInvitationServiceTest {
    @InjectMocks
    private AcceptInvitationService acceptInvitationService;

    @Mock
    private ApplicationEventPublisher publisher;
    @Mock
    private VerifyInvitationUseCase verifyInvitationUseCase;
    @Mock
    private CreateNotificationUseCase createNotificationUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;
    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("초대 수락 - 성공 (기존 알림 O, 알림 보내기 O)")
    void acceptInvitationSuccess() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long receiverId = 1L;
        CustomUserDetails receiver = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CustomUserDetails.class)
                .set("userId", receiverId)
                .sample();
        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", shareId)
                .set("folder.folderId", folderId)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();
        // 예전에 존재했던 알림 e.g) 재초대한 경우
        NotificationEntity existingNotification = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("folder.folderId", folderId)
                .set("folderShare.shareId", shareId)
                .set("sender.userId", receiverId)
                .sample();
        RequestNotificationWithFolderShareDto notification = RequestNotificationWithFolderShareDto.builder()
                .sender(folderShare.getTargetUser())
                .folder(folderShare.getFolder())
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();
        List<UserEntity> acceptedInFolderOtherUsers = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(UserEntity.class, 3);
        List<FolderShareEntity> acceptedInFolderOtherUsersShares = acceptedInFolderOtherUsers.stream()
                .map(user -> FixtureMonkey
                        .builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(FolderShareEntity.class)
                        .set("targetUser", user)
                        .set("folder.folderId", folderId)
                        .set("shareId", shareId)
                        .set("invitationStatus", InvitationStatus.ACCEPT)
                        .sample())
                .toList();
        
        Mockito
                .when(folderShareRepository.getShareInvitationWithFolder(folderId, receiverId))
                .thenReturn(Optional.of(folderShare));
        
        Mockito
                .doNothing()
                .when(verifyInvitationUseCase)
                .execute(Mockito.any(VerifyInvitationCommand.class));
        
        Mockito
                .when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);
        
        Mockito
                .when(notificationRepository.getReceivedNotification(folderId, shareId, receiverId))
                .thenReturn(Optional.of(existingNotification));
        
        Mockito
                .doNothing()
                .when(notificationRepository)
                .delete(Mockito.any(NotificationEntity.class));

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .when(folderShareRepository.getAllShareInvitationsWithoutMe(folderId, receiverId))
                .thenReturn(acceptedInFolderOtherUsersShares);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.anyList());

        acceptInvitationService.accept(folderId, shareId, receiver);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitationWithFolder(folderId, receiverId);

        Mockito
                .verify(verifyInvitationUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getReceivedNotification(folderId, shareId, receiverId);

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getAllShareInvitationsWithoutMe(folderId, receiverId);

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.anyList());
    }

    @Test
    @DisplayName("초대 수락 - 성공 (기존 알림 X, 알림 보내기 O)")
    void acceptInvitationSuccessNotExistingNotification() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long receiverId = 1L;
        CustomUserDetails receiver = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CustomUserDetails.class)
                .set("userId", receiverId)
                .sample();
        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", shareId)
                .set("folder.folderId", folderId)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();
        List<UserEntity> acceptedInFolderOtherUsers = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMe(UserEntity.class, 3);
        List<FolderShareEntity> acceptedInFolderOtherUsersShares = acceptedInFolderOtherUsers.stream()
                .map(user -> FixtureMonkey
                        .builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(FolderShareEntity.class)
                        .set("targetUser", user)
                        .set("folder.folderId", folderId)
                        .set("shareId", shareId)
                        .set("invitationStatus", InvitationStatus.ACCEPT)
                        .sample())
                .toList();

        Mockito
                .when(folderShareRepository.getShareInvitationWithFolder(folderId, receiverId))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .doNothing()
                .when(verifyInvitationUseCase)
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);

        Mockito
                .when(notificationRepository.getReceivedNotification(folderId, shareId, receiverId))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .when(folderShareRepository.getAllShareInvitationsWithoutMe(folderId, receiverId))
                .thenReturn(acceptedInFolderOtherUsersShares);

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.anyList());

        acceptInvitationService.accept(folderId, shareId, receiver);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitationWithFolder(folderId, receiverId);

        Mockito
                .verify(verifyInvitationUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getReceivedNotification(folderId, shareId, receiverId);

        Mockito
                .verify(notificationRepository, Mockito.times(0))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getAllShareInvitationsWithoutMe(folderId, receiverId);

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.anyList());
    }

    @Test
    @DisplayName("초대 수락 - 성공 (기존 알림 X, 알림 보내기 X)")
    void acceptInvitationSuccessNotExistingNotificationNoNotification() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long receiverId = 1L;
        CustomUserDetails receiver = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CustomUserDetails.class)
                .set("userId", receiverId)
                .sample();
        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", shareId)
                .set("folder.folderId", folderId)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        Mockito
                .when(folderShareRepository.getShareInvitationWithFolder(folderId, receiverId))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .doNothing()
                .when(verifyInvitationUseCase)
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);

        Mockito
                .when(notificationRepository.getReceivedNotification(folderId, shareId, receiverId))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(createNotificationUseCase)
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .when(folderShareRepository.getAllShareInvitationsWithoutMe(folderId, receiverId))
                .thenReturn(List.of());

        Mockito
                .doNothing()
                .when(publisher)
                .publishEvent(Mockito.anyList());

        acceptInvitationService.accept(folderId, shareId, receiver);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitationWithFolder(folderId, receiverId);

        Mockito
                .verify(verifyInvitationUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getReceivedNotification(folderId, shareId, receiverId);

        Mockito
                .verify(notificationRepository, Mockito.times(0))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(1))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getAllShareInvitationsWithoutMe(folderId, receiverId);

        Mockito
                .verify(publisher, Mockito.times(1))
                .publishEvent(Mockito.anyList());
    }

    @Test
    @DisplayName("초대 수락 - 실패 (초대 없음)")
    void acceptInvitationFailNotExistingInvitation() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long receiverId = 1L;
        CustomUserDetails receiver = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CustomUserDetails.class)
                .set("userId", receiverId)
                .sample();

        Mockito
                .when(folderShareRepository.getShareInvitationWithFolder(folderId, receiverId))
                .thenReturn(Optional.empty());

        Assertions.assertThatThrownBy(() -> acceptInvitationService.accept(folderId, shareId, receiver))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SHARE);

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .getShareInvitationWithFolder(folderId, receiverId);

        Mockito
                .verify(verifyInvitationUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .save(Mockito.any(FolderShareEntity.class));

        Mockito
                .verify(notificationRepository, Mockito.times(0))
                .getReceivedNotification(folderId, shareId, receiverId);

        Mockito
                .verify(notificationRepository, Mockito.times(0))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito
                .verify(createNotificationUseCase, Mockito.times(0))
                .execute(Mockito.any(CreateNotificationCommand.class));

        Mockito
                .verify(folderShareRepository, Mockito.times(0))
                .getAllShareInvitationsWithoutMe(folderId, receiverId);

        Mockito
                .verify(publisher, Mockito.times(0))
                .publishEvent(Mockito.anyList());
    }
}

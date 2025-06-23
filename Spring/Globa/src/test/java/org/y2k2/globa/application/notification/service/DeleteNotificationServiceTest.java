package org.y2k2.globa.application.notification.service;

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
import org.y2k2.globa.application.notification.command.VerifyModifyNotificationCommand;
import org.y2k2.globa.application.notification.usecase.VerifyModifyNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.domain.notificationread.repository.NotificationReadRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class DeleteNotificationServiceTest {
    @InjectMocks
    private DeleteNotificationService deleteNotificationService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private VerifyModifyNotificationUseCase verifyModifyNotificationUseCase;
    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private NotificationReadRepository notificationReadRepository;

    @Test
    @DisplayName("알림 삭제 - 성공 (공지)")
    void deleteNotification_Success_NoticeOrShare() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", user)
                .set("type", NotificationType.NOTICE)
                .set("notice.noticeId", 1L)
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(notificationReadRepository.getNotificationRead(notification.getNotificationId()))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(notificationReadRepository)
                .save(Mockito.any(NotificationReadEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), user.getUserId());

        Mockito.verify(notificationReadRepository, Mockito.times(1))
                .save(Mockito.any(NotificationReadEntity.class));

        Mockito.verify(notificationRepository, Mockito.never())
                .delete(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (공유 파일 추가)")
    void deleteNotification_Success_ShareFolderAddFile() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("folder", folder)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", user)
                .set("type", NotificationType.SHARE_FOLDER_ADD_FILE)
                .set("notice.noticeId", 1L)
                .set("folder", folder)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", user.getUserId())
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("record", record)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(notificationReadRepository.getNotificationRead(notification.getNotificationId()))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(notificationReadRepository)
                .save(Mockito.any(NotificationReadEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), user.getUserId());

        Mockito.verify(notificationReadRepository, Mockito.times(1))
                .save(Mockito.any(NotificationReadEntity.class));

        Mockito.verify(notificationRepository, Mockito.never())
                .delete(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (공유 사용자 추가)")
    void deleteNotification_Success_ShareFolderAddUser() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", user)
                .set("type", NotificationType.SHARE_FOLDER_ADD_USER)
                .set("notice.noticeId", 1L)
                .set("folder", folder)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", user.getUserId())
                .set("folderShare.targetUser.userId", 2L)
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("record", null)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(notificationReadRepository.getNotificationRead(notification.getNotificationId()))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(notificationReadRepository)
                .save(Mockito.any(NotificationReadEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), user.getUserId());

        Mockito.verify(notificationReadRepository, Mockito.times(1))
                .save(Mockito.any(NotificationReadEntity.class));

        Mockito.verify(notificationRepository, Mockito.never())
                .delete(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (댓글 추가)")
    void deleteNotification_Success_ShareFolderAddComment() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("folder", folder)
                .sample();

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", user)
                .set("type", NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .set("notice.noticeId", 1L)
                .set("folder", folder)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", user.getUserId())
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("record", record)
                .set("comment", comment)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito
                .when(notificationReadRepository.getNotificationRead(notification.getNotificationId()))
                .thenReturn(Optional.empty());

        Mockito
                .doNothing()
                .when(notificationReadRepository)
                .save(Mockito.any(NotificationReadEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), user.getUserId());

        Mockito.verify(notificationReadRepository, Mockito.times(1))
                .save(Mockito.any(NotificationReadEntity.class));

        Mockito.verify(notificationRepository, Mockito.never())
                .delete(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (공유 초대)")
    void deleteNotification_Success_Invitation() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        UserEntity receiver = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", sender)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("ownerUser", sender)
                .set("targetUser", receiver)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", sender)
                .set("receiver", receiver)
                .set("type", NotificationType.SHARE_FOLDER_INVITE)
                .set("notice", null)
                .set("folder", folder)
                .set("folderShare", folderShare)
                .set("record", null)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(receiver.getUserId()))
                .thenReturn(receiver);

        Mockito
                .doNothing()
                .when(notificationRepository)
                .delete(Mockito.any(NotificationEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), receiver.getUserId());

        Mockito.verify(notificationRepository, Mockito.times(1))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito.verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (업로드 성공)")
    void deleteNotification_Success_UploadSuccess() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", sender)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("ownerUser", sender)
                .set("targetUser", sender)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("folder", folder)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", sender)
                .set("receiver", sender)
                .set("type", NotificationType.UPLOAD_SUCCESS)
                .set("notice", null)
                .set("folder", folder)
                .set("folderShare", folderShare)
                .set("record", record)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(sender.getUserId()))
                .thenReturn(sender);

        Mockito
                .doNothing()
                .when(notificationRepository)
                .delete(Mockito.any(NotificationEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), sender.getUserId());

        Mockito.verify(notificationRepository, Mockito.times(1))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito.verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (업로드 실패)")
    void deleteNotification_Success_UploadFailure() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", sender)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("ownerUser", sender)
                .set("targetUser", sender)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", sender)
                .set("receiver", sender)
                .set("type", NotificationType.UPLOAD_FAILED)
                .set("notice", null)
                .set("folder", folder)
                .set("folderShare", folderShare)
                .set("record", null)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(sender.getUserId()))
                .thenReturn(sender);

        Mockito
                .doNothing()
                .when(notificationRepository)
                .delete(Mockito.any(NotificationEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), sender.getUserId());

        Mockito.verify(notificationRepository, Mockito.times(1))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito.verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 성공 (문의 답변)")
    void deleteNotification_Success_Inquiry() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        UserEntity receiver = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .sample();

        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("inquiryId", 1L)
                .set("user", sender)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("sender", sender)
                .set("receiver", receiver)
                .set("type", NotificationType.INQUIRY)
                .set("notice", null)
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("comment", null)
                .set("inquiry", inquiry)
                .sample();

        Mockito
                .when(notificationRepository.getNotification(notification.getNotificationId()))
                .thenReturn(Optional.of(notification));

        Mockito
                .doNothing()
                .when(verifyModifyNotificationUseCase)
                .execute(Mockito.any(VerifyModifyNotificationCommand.class));

        Mockito
                .when(findUserUseCase.execute(sender.getUserId()))
                .thenReturn(sender);

        Mockito
                .doNothing()
                .when(notificationRepository)
                .delete(Mockito.any(NotificationEntity.class));

        deleteNotificationService.delete(notification.getNotificationId(), sender.getUserId());

        Mockito.verify(notificationRepository, Mockito.times(1))
                .delete(Mockito.any(NotificationEntity.class));

        Mockito.verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }

    @Test
    @DisplayName("알림 삭제 - 실패 (알림 X)")
    void deleteNotification_Failure_NotFound() {
        Long notificationId = 1L;
        Long userId = 1L;

        Mockito
                .when(notificationRepository.getNotification(notificationId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> deleteNotificationService.delete(notificationId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_NOTIFICATION);

        Mockito.verify(notificationRepository, Mockito.never())
                .delete(Mockito.any(NotificationEntity.class));

        Mockito.verify(notificationReadRepository, Mockito.never())
                .save(Mockito.any(NotificationReadEntity.class));
    }
}

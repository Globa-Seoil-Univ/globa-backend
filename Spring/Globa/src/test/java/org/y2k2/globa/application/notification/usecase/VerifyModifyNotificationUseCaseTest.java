package org.y2k2.globa.application.notification.usecase;

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
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;

@ExtendWith(MockitoExtension.class)
public class VerifyModifyNotificationUseCaseTest {
    @InjectMocks
    private VerifyModifyNotificationUseCase verifyModifyNotificationUseCase;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (공지)")
    void verifyModifyNotificationSuccess() {
        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.NOTICE)
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("inquiry", null)
                .set("comment", null)
                .sample();

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, 1L)
        );

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (공유 초대 [일반])")
    void verifyModifyNotificationSuccessForGeneral() {
        Long senderId = 1L,
                receiverId = 2L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.SHARE_FOLDER_INVITE)
                .set("sender.userId", senderId)
                .set("receiver.userId", receiverId)
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("inquiry", null)
                .set("comment", null)
                .sample();

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, receiverId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (업로드 성공 [일반])")
    void verifyModifyNotificationSuccessForUploadSuccess() {
        Long userId = 1L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.UPLOAD_SUCCESS)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("inquiry", null)
                .set("comment", null)
                .sample();

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, userId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (업로드 실패 [일반])")
    void verifyModifyNotificationSuccessForUploadFailed() {
        Long userId = 1L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.UPLOAD_FAILED)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("inquiry", null)
                .set("comment", null)
                .sample();

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, userId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (문의 답변 [일반])")
    void verifyModifyNotificationSuccessForInquiry() {
        Long senderId = 1L,
                receiverId = 2L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.INQUIRY)
                .set("sender.userId", senderId)
                .set("receiver.userId", receiverId)
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("comment", null)
                .sample();

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, receiverId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 실패 (일반 [초대, 업로드 성공, 업로드 실패, 문의 답변])")
    void verifyModifyNotificationFailureForGeneral() {
        Long senderId = 1L,
                receiverId = 2L;

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.INQUIRY)
                .set("sender.userId", senderId)
                .set("receiver.userId", senderId) // receiverId와 senderId가 다름
                .set("folder", null)
                .set("folderShare", null)
                .set("record", null)
                .set("comment", null)
                .sample();

        Assertions
                .assertThatThrownBy(() -> verifyModifyNotificationUseCase.execute(
                        new VerifyModifyNotificationCommand(notification, receiverId)
                ))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);

        Mockito
                .verify(folderShareRepository, Mockito.never())
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권함 검증 - 성공 (사용자 추가 [공유])")
    void verifyModifyNotificationSuccessForShareAddUser() {
        Long userId = 2L;

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user.userId", userId)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user.userId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_USER)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("folder", folder)
                .set("record", record)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", 1L)
                .set("folderShare.targetUser.userId", userId)
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(folderShareRepository.isAccessible(userId, folder.getFolderId()))
                .thenReturn(true);

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, userId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (파일 추가 [공유])")
    void verifyModifyNotificationSuccessForShareAddFile() {
        Long userId = 2L;

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user.userId", userId)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user.userId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_FILE)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("folder", folder)
                .set("record", record)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", 1L)
                .set("folderShare.targetUser.userId", userId)
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(folderShareRepository.isAccessible(userId, folder.getFolderId()))
                .thenReturn(true);

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, userId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 성공 (댓글 추가 [공유])")
    void verifyModifyNotificationSuccessForShareAddComment() {
        Long userId = 2L;

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user.userId", userId)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user.userId", 1L)
                .sample();

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", 1L)
                .set("user.userId", userId)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("folder", folder)
                .set("record", record)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", 1L)
                .set("folderShare.targetUser.userId", userId)
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("comment", comment)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(folderShareRepository.isAccessible(userId, folder.getFolderId()))
                .thenReturn(true);

        verifyModifyNotificationUseCase.execute(
                new VerifyModifyNotificationCommand(notification, userId)
        );

        Mockito
                .verify(folderShareRepository, Mockito.times(1))
                .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }

    @Test
    @DisplayName("알림 수정 권한 검증 - 실패 (공유 알림 [공유 권한 없음])")
    void verifyModifyNotificationFailureForShare() {
        Long userId = 2L;

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user.userId", userId)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user.userId", 1L)
                .sample();

        NotificationEntity notification = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NotificationEntity.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_FILE)
                .set("sender.userId", userId)
                .set("receiver.userId", userId)
                .set("folder", folder)
                .set("record", record)
                .set("folderShare.folder.folderId", folder.getFolderId())
                .set("folderShare.ownerUser.userId", 1L)
                .set("folderShare.targetUser.userId", userId)
                .set("folderShare.invitationStatus", InvitationStatus.ACCEPT)
                .set("comment", null)
                .set("inquiry", null)
                .sample();

        Mockito
                .when(folderShareRepository.isAccessible(999L, folder.getFolderId()))
                .thenReturn(false);

        Assertions
            .assertThatThrownBy(() -> verifyModifyNotificationUseCase.execute(
                    new VerifyModifyNotificationCommand(notification, 999L)) // 존재하지 않는 사용자 ID
            )
            .isInstanceOf(CustomException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION);

        Mockito
            .verify(folderShareRepository, Mockito.times(1))
            .isAccessible(Mockito.anyLong(), Mockito.anyLong());
    }
}

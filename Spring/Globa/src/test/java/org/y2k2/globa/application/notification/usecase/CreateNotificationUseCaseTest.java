package org.y2k2.globa.application.notification.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.*;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class CreateNotificationUseCaseTest {
    @InjectMocks
    private CreateNotificationUseCase createNotificationUseCase;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 생성 - 성공 (공지)")
    void createNotificationSuccessNotice() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        NoticeEntity notice = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(NoticeEntity.class)
                .set("noticeId", 1L)
                .set("user", sender)
                .sample();

        RequestNotificationWithTopicDto message = RequestNotificationWithTopicDto.builder()
                .sender(sender)
                .title("공지사항 제목")
                .body("공지사항 내용")
                .notice(notice)
                .notificationType(NotificationType.NOTICE)
                .topic(FcmTopic.NOTICE.getTopic())
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (업로드 성공)")
    void createNotificationSuccessUpload() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user", sender)
                .sample();

        RequestNotificationWithUploadSuccessDto message = RequestNotificationWithUploadSuccessDto.builder()
                .sender(sender)
                .receiver(sender)
                .title("업로드 성공")
                .body("파일이 성공적으로 업로드되었습니다.")
                .record(record)
                .notificationType(NotificationType.UPLOAD_SUCCESS)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (업로드 실패)")
    void createNotificationSuccessUploadFailed() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        SendMessage message = SendMessage.builder()
                .sender(sender)
                .receiver(sender)
                .title("업로드 실패")
                .body("파일 업로드에 실패했습니다.")
                .notificationType(NotificationType.UPLOAD_FAILED)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (공유 파일 추가)")
    void createNotificationSuccessShareFolderAddFile() {
        UserEntity owner = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("isDeleted", false)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", owner)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", 1L)
                .set("ownerUser", owner)
                .set("targetUser", sender)
                .set("folder", folder)
                .sample();

        RequestNotificationWithFolderShareDto message = RequestNotificationWithFolderShareDto.builder()
                .sender(sender)
                .title("공유 폴더에 파일이 추가되었습니다.")
                .body("새로운 파일이 공유 폴더에 추가되었습니다.")
                .folder(folder)
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (공유 폴더 사용자 추가)")
    void createNotificationSuccessShareFolderAddUser() {
        UserEntity owner = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("isDeleted", false)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", owner)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", 1L)
                .set("ownerUser", owner)
                .set("targetUser", sender)
                .set("folder", folder)
                .sample();

        RequestNotificationWithFolderShareDto message = RequestNotificationWithFolderShareDto.builder()
                .sender(sender)
                .title("새로운 사용자가 공유 폴더에 추가되었습니다.")
                .body("새로운 사용자가 공유 폴더에 추가되었습니다.")
                .folder(folder)
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (공유 폴더 댓글 추가)")
    void createNotificationSuccessShareFolderAddComment() {
        UserEntity owner = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("isDeleted", false)
                .sample();

        FolderEntity folder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("user", owner)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleId", 1L)
                .set("roleName", FolderRole.EDITOR)
                .sample();

        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", 1L)
                .set("ownerUser", owner)
                .set("targetUser", sender)
                .set("role", folderRole)
                .set("folder", folder)
                .sample();

        RecordEntity record = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RecordEntity.class)
                .set("recordId", 1L)
                .set("user", owner)
                .sample();

        SectionEntity section = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(SectionEntity.class)
                .set("sectionId", 1L)
                .set("record", record)
                .sample();

        HighlightEntity highlight = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(HighlightEntity.class)
                .set("highlightId", 1L)
                .set("section", section)
                .sample();

        CommentEntity comment = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(CommentEntity.class)
                .set("commentId", 1L)
                .set("user", sender)
                .set("content", "새로운 댓글이 추가되었습니다.")
                .set("highlight", highlight)
                .sample();

        RequestNotificationWithFolderShareCommentDto message = RequestNotificationWithFolderShareCommentDto.builder()
                .sender(sender)
                .title("새로운 사용자가 공유 폴더에 추가되었습니다.")
                .body("새로운 사용자가 공유 폴더에 추가되었습니다.")
                .folder(folder)
                .folderShare(folderShare)
                .record(record)
                .comment(comment)
                .notificationType(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (문의)")
    void createNotificationSuccessInquiry() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        UserEntity receiver = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("isDeleted", false)
                .sample();

        InquiryEntity inquiry = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(InquiryEntity.class)
                .set("inquiryId", 1L)
                .set("user", receiver)
                .sample();

        RequestNotificationWithInquiryDto message = RequestNotificationWithInquiryDto.builder()
                .sender(sender)
                .receiver(receiver)
                .title("문의 답변이 도착했습니다.")
                .body("문의하신 내용에 대한 답변이 도착했습니다.")
                .notificationType(NotificationType.INQUIRY)
                .inquiry(inquiry)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }

    @Test
    @DisplayName("알림 생성 - 성공 (공유 초대)")
    void createNotificationSuccessShareFolderInvite() {
        UserEntity sender = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        UserEntity receiver = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("isDeleted", false)
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
                .set("shareId", 1L)
                .set("ownerUser", sender)
                .set("targetUser", receiver)
                .set("folder", folder)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        RequestNotificationWithInvitationDto message = RequestNotificationWithInvitationDto.builder()
                .sender(sender)
                .receiver(receiver)
                .title("공유 폴더 초대")
                .body("새로운 공유 폴더에 초대되었습니다.")
                .folder(folder)
                .folderShare(folderShare)
                .notificationType(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        Mockito
                .doNothing()
                .when(notificationRepository)
                .save(Mockito.any(NotificationEntity.class));

        createNotificationUseCase.execute(new CreateNotificationCommand(message));

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .save(Mockito.any(NotificationEntity.class));
    }
}

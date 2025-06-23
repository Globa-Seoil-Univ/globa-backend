package org.y2k2.globa.infrastructure.persistence.notification.repository;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.notification.dto.common.NotificationParameters;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.fixture.comment.CommentFixture;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.highlight.HighlightFixture;
import org.y2k2.globa.fixture.inquiry.InquiryFixture;
import org.y2k2.globa.fixture.notice.NoticeFixture;
import org.y2k2.globa.fixture.notification.NotificationFixture;
import org.y2k2.globa.fixture.notificationread.NotificationReadFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.config.RepositoryIntegrationTest;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjection;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@RepositoryIntegrationTest
public class NotificationRepositoryTest {
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private NotificationTestRepositoryImpl saveRepository;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private SectionFixture sectionFixture;
    @Autowired
    private HighlightFixture highlightFixture;
    @Autowired
    private CommentFixture commentFixture;
    @Autowired
    private InquiryFixture inquiryFixture;
    @Autowired
    private NoticeFixture noticeFixture;
    @Autowired
    private NotificationReadFixture notificationReadFixture;

    private UserEntity user;
    private UserEntity otherUser;
    private FolderEntity folder;
    private FolderShareEntity editorShare;
    private RecordEntity record;
    private CommentEntity comment;
    private InquiryEntity inquiry;
    private NoticeEntity notice;

    @BeforeEach
    void setup() {
        user = userFixture.save(
                UserFixture.builder().build()
        );

        otherUser = userFixture.save(
                UserFixture.builder()
                        .name("otherUser")
                        .build()
        );

        folder = folderFixture.save(
                FolderFixture.builder()
                        .user(user)
                        .build()
        );

        FolderRoleEntity owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        FolderRoleEntity editor = folderRoleFixture.getEntity(FolderRole.EDITOR);

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(folder)
                        .role(owner)
                        .owner(user)
                        .target(user)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        editorShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(folder)
                        .role(editor)
                        .owner(user)
                        .target(otherUser)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        record = recordFixture.save(
                RecordFixture.builder()
                        .folder(folder)
                        .user(user)
                        .build()
        );

        SectionEntity section = sectionFixture.save(
                SectionFixture.builder()
                        .record(record)
                        .build()
        );

        HighlightEntity highlight = highlightFixture.save(
                HighlightFixture.builder()
                        .section(section)
                        .build()
        );

        comment = commentFixture.save(
                CommentFixture.builder()
                        .highlight(highlight)
                        .user(user)
                        .build()
        );

        inquiry = inquiryFixture.save(
                InquiryFixture.builder()
                        .user(user)
                        .build()
        );

        notice = noticeFixture.save(
                NoticeFixture.builder()
                        .user(user)
                        .build()
        );
    }

    @Test
    @DisplayName("알림 생성 - 성공 (공지)")
    void saveNotification() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .notice(notice)
                .type(NotificationType.NOTICE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getNotice())
                .isEqualTo(notice);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.NOTICE);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (폴더 공유 초대)")
    void saveNotificationShareInvite() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getReceiver())
                .isEqualTo(otherUser);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getFolderShare())
                .isEqualTo(editorShare);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_INVITE);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (폴더 공유 파일 추가)")
    void saveNotificationShareAddFile() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getRecord())
                .isEqualTo(record);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_ADD_FILE);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (사용자 초대 수락)")
    void saveNotificationShareAddUser() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getReceiver())
                .isEqualTo(otherUser);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getFolderShare())
                .isEqualTo(editorShare);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_ADD_USER);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (폴더 공유 댓글 추가)")
    void saveNotificationShareAddComment() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .comment(comment)
                .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getComment())
                .isEqualTo(comment);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_ADD_COMMENT);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (업로드 성공)")
    void saveNotificationUploadSuccess() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.UPLOAD_SUCCESS)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getReceiver())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getRecord())
                .isEqualTo(record);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.UPLOAD_SUCCESS);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (업로드 실패)")
    void saveNotificationUploadFailed() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .type(NotificationType.UPLOAD_FAILED)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getReceiver())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getFolder())
                .isEqualTo(folder);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.UPLOAD_FAILED);
    }

    @Test
    @DisplayName("알림 생성 - 성공 (문의)")
    void saveNotificationInquiry() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .inquiry(inquiry)
                .type(NotificationType.INQUIRY)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Assertions
                .assertThat(savedNotification)
                .isNotNull()
                .extracting(NotificationEntity::getNotificationId)
                .isNotNull();

        Assertions
                .assertThat(savedNotification.getSender())
                .isEqualTo(user);

        Assertions
                .assertThat(savedNotification.getReceiver())
                .isEqualTo(otherUser);

        Assertions
                .assertThat(savedNotification.getInquiry())
                .isEqualTo(inquiry);

        Assertions
                .assertThat(savedNotification.getType())
                .isEqualTo(NotificationType.INQUIRY);
    }

    @Test
    @DisplayName("알림 다중 생성 - 성공")
    void saveAllNotifications() {
        NotificationEntity notification1 = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity notification2 = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        List<NotificationEntity> notifications = List.of(notification1, notification2);
        List<NotificationEntity> savedNotifications = saveRepository.saveAll(notifications);

        Assertions
                .assertThat(savedNotifications)
                .hasSize(2);

        Assertions
                .assertThat(savedNotifications.get(0).getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_INVITE);

        Assertions
                .assertThat(savedNotifications.get(1).getType())
                .isEqualTo(NotificationType.SHARE_FOLDER_ADD_FILE);
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    void deleteNotification() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationRepository.delete(savedNotification);

        Assertions
                .assertThat(notificationRepository.getNotification(savedNotification.getNotificationId()))
                .isEmpty();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (공지 알림, 읽음 X)")
    void hasUnReadNotificationNotice() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .notice(notice)
                .type(NotificationType.NOTICE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (공지 알림, 읽음 O)")
    void hasUnReadNotificationNoticeRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .notice(notice)
                .type(NotificationType.NOTICE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 초대 알림, 읽음 X)")
    void hasUnReadNotificationShareInvite() {
        FolderShareEntity readerShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(folder)
                        .role(folderRoleFixture.getEntity(FolderRole.READER))
                        .owner(user)
                        .target(otherUser)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(readerShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 초대 알림, 읽음 O)")
    void hasUnReadNotificationShareInviteRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 파일 추가 알림, 읽음 X)")
    void hasUnReadNotificationShareAddFile() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 파일 추가 알림, 읽음 O)")
    void hasUnReadNotificationShareAddFileRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 사용자 추가 알림, 읽음 X)")
    void hasUnReadNotificationShareAddUser() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (폴더 공유 사용자 추가 알림, 읽음 O)")
    void hasUnReadNotificationShareAddUserRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (댓글 알림, 읽음 X)")
    void hasUnReadNotification() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .folderShare(editorShare)
                .record(record)
                .comment(comment)
                .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (댓글 알림, 읽음 O)")
    void hasUnReadNotificationRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .folderShare(editorShare)
                .record(record)
                .comment(comment)
                .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (업로드 성공 알림, 읽음 X)")
    void hasUnReadNotificationUploadSuccess() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.UPLOAD_SUCCESS)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(user.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (업로드 성공 알림, 읽음 O)")
    void hasUnReadNotificationUploadSuccessRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.UPLOAD_SUCCESS)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(user.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (업로드 실패 알림, 읽음 X)")
    void hasUnReadNotificationUploadFailed() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .type(NotificationType.UPLOAD_FAILED)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(user.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (업로드 실패 알림, 읽음 O)")
    void hasUnReadNotificationUploadFailedRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .type(NotificationType.UPLOAD_FAILED)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(user.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (문의 알림, 읽음 X)")
    void hasUnReadNotificationInquiry() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .inquiry(inquiry)
                .type(NotificationType.INQUIRY)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isTrue();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (문의 알림, 읽음 O)")
    void hasUnReadNotificationInquiryRead() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .inquiry(inquiry)
                .type(NotificationType.INQUIRY)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 읽음 여부 확인 - 성공 (삭제된 알림 읽음)")
    void hasUnReadNotificationDeleted() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(savedNotification)
                        .user(otherUser)
                        .isDeleted(true)
                        .build()
        );

        Boolean hasUnRead = notificationRepository.hasUnReadNotification(otherUser.getUserId());
        Assertions.assertThat(hasUnRead).isFalse();
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공지)")
    void getNotifications() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .notice(notice)
                .type(NotificationType.NOTICE)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        NotificationParameters parameters = new NotificationParameters();
        parameters.setNotice(true);

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                otherUser.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .hasSize(1);

        Assertions
                .assertThat(notifications.getContent().get(0).getNotificationId())
                .isEqualTo(savedNotification.getNotificationId());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (폴더 공유 초대)")
    void getNotificationsShareInvite() {
        UserEntity otherUser2 = userFixture.save(
                UserFixture.builder()
                        .name("otherUser2")
                        .build()
        );
        FolderShareEntity readerShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(folder)
                        .role(folderRoleFixture.getEntity(FolderRole.READER))
                        .owner(user)
                        .target(otherUser2)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser2)
                .folder(folder)
                .folderShare(readerShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        NotificationEntity savedNotification = saveRepository.save(notification);

        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        NotificationParameters parameters = new NotificationParameters();
        parameters.setInvite(true);

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                otherUser2.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .hasSize(1);

        Assertions
                .assertThat(notifications.getContent().get(0).getNotificationId())
                .isEqualTo(savedNotification.getNotificationId());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공유 관련 알림)")
    void getNotificationsShareRelated() {
        // 파일 추가, 사용자 추가, 댓글 추가 알림을 포함한 테스트
        NotificationEntity notification1 = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        NotificationEntity notification2 = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        NotificationEntity notification3 = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .folderShare(editorShare)
                .record(record)
                .comment(comment)
                .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<NotificationEntity> savedNotifications = saveRepository.saveAll(List.of(notification1, notification2, notification3));
        log.info("Saved Notifications = {}, {}, {}",
                savedNotifications.get(0).getNotificationId(),
                savedNotifications.get(1).getNotificationId(),
                savedNotifications.get(2).getNotificationId());

        NotificationParameters parameters = new NotificationParameters();
        parameters.setShare(true);

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                otherUser.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .hasSize(3);

        // 3개 확인
        Assertions
                .assertThat(notifications.getContent())
                .extracting(NotificationProjection::getNotificationId)
                .containsExactlyInAnyOrder(
                        savedNotifications.get(0).getNotificationId(),
                        savedNotifications.get(1).getNotificationId(),
                        savedNotifications.get(2).getNotificationId()
                );
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (업로드 관련 알림)")
    void getNotificationsUploadRelated() {
        NotificationEntity notification1 = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.UPLOAD_SUCCESS)
                .build();

        NotificationEntity notification2 = NotificationFixture.builder()
                .sender(user)
                .receiver(user)
                .folder(folder)
                .type(NotificationType.UPLOAD_FAILED)
                .build();

        Pageable pageable = PageRequest.of(0, 10);

        List<NotificationEntity> savedNotifications = saveRepository.saveAll(List.of(notification1, notification2));
        log.info("Saved Notifications = {}, {}", savedNotifications.get(0).getNotificationId(), savedNotifications.get(1).getNotificationId());

        NotificationParameters parameters = new NotificationParameters();
        parameters.setRecord(true);

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                user.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .hasSize(2);

        // 2개 확인
        Assertions
                .assertThat(notifications.getContent())
                .extracting(NotificationProjection::getNotificationId)
                .containsExactlyInAnyOrder(
                        savedNotifications.get(0).getNotificationId(),
                        savedNotifications.get(1).getNotificationId()
                );
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (문의 관련 알림)")
    void getNotificationsInquiryRelated() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .inquiry(inquiry)
                .type(NotificationType.INQUIRY)
                .build();
        Pageable pageable = PageRequest.of(0, 10);

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        NotificationParameters parameters = new NotificationParameters();
        parameters.setInquiry(true);

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                otherUser.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .hasSize(1);

        Assertions
                .assertThat(notifications.getContent().get(0).getNotificationId())
                .isEqualTo(savedNotification.getNotificationId());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (빈 목록)")
    void getNotificationsEmpty() {
        Pageable pageable = PageRequest.of(0, 10);
        NotificationParameters parameters = new NotificationParameters();

        Page<NotificationProjection> notifications = notificationRepository.getNotifications(
                otherUser.getUserId(),
                parameters,
                pageable
        );

        Assertions
                .assertThat(notifications)
                .isNotNull()
                .isEmpty();
    }

    @Test
    @DisplayName("특정 폴더에 대한 알림 조회 - 성공")
    void getNotificationsByFolder() {
        // 초대 알림
        UserEntity otherUser2 = userFixture.save(
                UserFixture.builder()
                        .name("otherUser2")
                        .build()
        );
        FolderShareEntity share = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(folder)
                        .role(folderRoleFixture.getEntity(FolderRole.READER))
                        .owner(user)
                        .target(otherUser2)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser2)
                .folder(folder)
                .folderShare(share)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Optional<NotificationEntity> retrievedNotification = notificationRepository.getReceivedNotification(
                folder.getFolderId(),
                share.getShareId(),
                otherUser2.getUserId()
        );

        Assertions
                .assertThat(retrievedNotification)
                .isPresent()
                .get()
                .extracting(NotificationEntity::getNotificationId)
                .isEqualTo(savedNotification.getNotificationId());
    }

    @Test
    @DisplayName("알림 조회 - 성공")
    void getNotification() {
        NotificationEntity notification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        NotificationEntity savedNotification = saveRepository.save(notification);
        log.info("Saved Notification = {}, {}", savedNotification.getNotificationId(), savedNotification.getType());

        Optional<NotificationEntity> retrievedNotification = notificationRepository.getNotification(savedNotification.getNotificationId());

        Assertions
                .assertThat(retrievedNotification)
                .isPresent()
                .get()
                .extracting(NotificationEntity::getNotificationId)
                .isEqualTo(savedNotification.getNotificationId());
    }

    @Test
    @DisplayName("안 읽은 알림 수 조회 - 성공 (모든 알림)")
    void countUnReadNotifications() {
        // 공지 알림
        NotificationEntity noticeNotification = NotificationFixture.builder()
                .sender(user)
                .notice(notice)
                .type(NotificationType.NOTICE)
                .build();

        // 폴더 공유 초대 알림
        FolderEntity otherFolder = folderFixture.save(
                FolderFixture.builder()
                        .user(user)
                        .build()
        );
        FolderShareEntity otherEditorShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(otherFolder)
                        .role(folderRoleFixture.getEntity(FolderRole.EDITOR))
                        .owner(user)
                        .target(otherUser)
                        .status(InvitationStatus.PENDING)
                        .build()
        );
        NotificationEntity shareInviteNotification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(otherFolder)
                .folderShare(otherEditorShare)
                .type(NotificationType.SHARE_FOLDER_INVITE)
                .build();

        // 폴더 공유 파일 추가 알림
        NotificationEntity shareAddFileNotification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .record(record)
                .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                .build();

        // 폴더 공유 사용자 추가 알림
        NotificationEntity shareAddUserNotification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .folder(folder)
                .folderShare(editorShare)
                .type(NotificationType.SHARE_FOLDER_ADD_USER)
                .build();

        // 폴더 공유 댓글 추가 알림
        NotificationEntity shareAddCommentNotification = NotificationFixture.builder()
                .sender(user)
                .folder(folder)
                .folderShare(editorShare)
                .record(record)
                .comment(comment)
                .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                .build();

        // 업로드 성공 알림
        NotificationEntity uploadSuccessNotification = NotificationFixture.builder()
                .sender(otherUser)
                .receiver(otherUser)
                .folder(folder)
                .record(record)
                .type(NotificationType.UPLOAD_SUCCESS)
                .build();

        // 업로드 실패 알림
        NotificationEntity uploadFailedNotification = NotificationFixture.builder()
                .sender(otherUser)
                .receiver(otherUser)
                .folder(folder)
                .type(NotificationType.UPLOAD_FAILED)
                .build();

        // 문의 알림
        NotificationEntity inquiryNotification = NotificationFixture.builder()
                .sender(user)
                .receiver(otherUser)
                .inquiry(inquiry)
                .type(NotificationType.INQUIRY)
                .build();

        saveRepository.saveAll(List.of(
                noticeNotification,
                shareInviteNotification,
                shareAddFileNotification,
                shareAddUserNotification,
                shareAddCommentNotification,
                uploadSuccessNotification,
                uploadFailedNotification,
                inquiryNotification
        ));

        NotificationUnReadCountProjection unReadCountProjection = notificationRepository.getUnReadCount(
                otherUser.getUserId()
        );

        Assertions
                .assertThat(unReadCountProjection)
                .isNotNull();

        Assertions
                .assertThat(unReadCountProjection.getNoticeCount())
                .isEqualTo(1);

        Assertions
                .assertThat(unReadCountProjection.getShareCount())
                .isEqualTo(3);

        Assertions
                .assertThat(unReadCountProjection.getRecordCount())
                .isEqualTo(2);

        Assertions
                .assertThat(unReadCountProjection.getInquiryCount())
                .isEqualTo(1);
    }
}

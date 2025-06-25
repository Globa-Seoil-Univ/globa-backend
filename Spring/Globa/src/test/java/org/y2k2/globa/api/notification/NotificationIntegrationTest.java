package org.y2k2.globa.api.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.notification.dto.common.NotificationDto;
import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadCountDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadNotificationDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.NotificationSort;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.answer.AnswerFixture;
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
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
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

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class NotificationIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleFixture roleFixture;
    @Autowired
    private UserRoleFixture userRoleFixture;
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
    private NotificationFixture notificationFixture;
    @Autowired
    private NotificationReadFixture notificationReadFixture;
    @Autowired
    private NoticeFixture noticeFixture;
    @Autowired
    private InquiryFixture inquiryFixture;
    @Autowired
    private AnswerFixture answerFixture;

    UserEntity myUser;
    UserEntity otherUser;
    FolderEntity myFolder;
    FolderShareEntity myFolderShare;
    FolderShareEntity otherFolderShare;
    RecordEntity myRecord;
    RecordEntity dummyRecord;
    NoticeEntity notice;
    CommentEntity comment;
    InquiryEntity inquiry;
    AnswerEntity answer;

    @BeforeEach
    void setup() {
        myUser = userFixture.save(
                UserFixture.builder()
                        .name("My User")
                        .isDeleted(false)
                        .build()
        );
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(roleFixture.getEntity(UserRole.ADMIN))
                        .build()
        );

        otherUser = userFixture.save(
                UserFixture.builder()
                        .name("Other User")
                        .isDeleted(false)
                        .build()
        );
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(otherUser)
                        .role(roleFixture.getEntity(UserRole.USER))
                        .build()
        );

        myFolder = folderFixture.save(
                FolderFixture.builder()
                        .title("My Folder")
                        .user(myUser)
                        .build()
        );

        myRecord = recordFixture.save(
                RecordFixture.builder()
                        .title("My Record")
                        .user(myUser)
                        .folder(myFolder)
                        .build()
        );
        dummyRecord = recordFixture.save(
                RecordFixture.builder()
                        .title("Dummy Record")
                        .user(myUser)
                        .folder(myFolder)
                        .build()
        );

        myFolderShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(myUser)
                        .role(folderRoleFixture.getEntity(FolderRole.OWNER))
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        otherFolderShare = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(otherUser)
                        .role(folderRoleFixture.getEntity(FolderRole.EDITOR))
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        notice = noticeFixture.save(
                NoticeFixture.builder()
                        .title("Notice Title")
                        .content("Notice Content")
                        .user(myUser)
                        .thumbnail("https://example.com/thumbnail.jpg")
                        .bgColor("#FFFFFF")
                        .build()
        );

        SectionEntity section = sectionFixture.save(
                SectionFixture.builder()
                        .title("Section Title")
                        .record(myRecord)
                        .build()
        );

        HighlightEntity highlight = highlightFixture.save(
                HighlightFixture.builder()
                        .section(section)
                        .build()
        );

        comment = commentFixture.save(
                CommentFixture.builder()
                        .content("This is a comment.")
                        .user(otherUser)
                        .highlight(highlight)
                        .deleted(false)
                        .build()
        );

        inquiry = inquiryFixture.save(
                InquiryFixture.builder()
                        .title("Inquiry Title")
                        .content("Inquiry Content")
                        .isSolved(true)
                        .user(myUser)
                        .build()
        );
        answer = answerFixture.save(
                AnswerFixture.builder()
                        .title("Answer Title")
                        .content("This is an answer.")
                        .inquiry(inquiry)
                        .user(myUser)
                        .build()
        );

        setSecurityContext(myUser);
    }

    private void createAllNotifications() {
        List<NotificationEntity> notifications = new ArrayList<>();

        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                    .build()
        );

        UserEntity otherUser2 = userFixture.save(
                UserFixture.builder()
                        .name("Other User 2")
                        .isDeleted(false)
                        .build()
        );
        FolderEntity otherFolder = folderFixture.save(
                FolderFixture.builder()
                        .title("Other User's Folder")
                        .user(otherUser2)
                        .build()
        );
        FolderShareEntity otherFolderShare2 = folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(otherFolder)
                        .owner(otherUser2)
                        .target(myUser)
                        .role(folderRoleFixture.getEntity(FolderRole.EDITOR))
                        .status(InvitationStatus.PENDING)
                        .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.SHARE_FOLDER_INVITE)
                        .sender(otherUser)
                        .receiver(myUser)
                        .folder(otherFolder)
                        .folderShare(otherFolderShare2)
                    .build()
        );

        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.SHARE_FOLDER_ADD_FILE)
                        .sender(otherUser)
                        .receiver(otherUser)
                        .folder(myFolder)
                        .folderShare(otherFolderShare)
                    .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.SHARE_FOLDER_ADD_USER)
                        .sender(otherUser)
                        .folder(myFolder)
                        .folderShare(otherFolderShare)
                    .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.SHARE_FOLDER_ADD_COMMENT)
                        .sender(otherUser)
                        .folder(myFolder)
                        .folderShare(otherFolderShare)
                        .record(myRecord)
                        .comment(comment)
                    .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.UPLOAD_SUCCESS)
                        .sender(myUser)
                        .receiver(myUser)
                        .folder(myFolder)
                        .folderShare(myFolderShare)
                        .record(dummyRecord)
                    .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.UPLOAD_FAILED)
                        .sender(myUser)
                        .receiver(myUser)
                        .folder(myFolder)
                        .folderShare(myFolderShare)
                        .record(dummyRecord)
                    .build()
        );
        notifications.add(
                NotificationFixture.builder()
                        .type(NotificationType.INQUIRY)
                        .sender(myUser)
                        .receiver(myUser)
                        .inquiry(inquiry)
                    .build()
        );

        notificationFixture.saveAll(notifications);
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (전체)")
    @WithAccount
    void getNotifications_Success() throws Exception {
        String type = NotificationSort.ALL.getValue();
        int page = 1,
                count = 10;

        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);
        log.info("type = {}", response.notifications().stream().map(
                NotificationDto::getType
        ).toList());

        Assertions
                .assertThat(response.total())
                .isEqualTo(8);

        Character[] types = {
                NotificationType.NOTICE.getTypeId(),
                NotificationType.SHARE_FOLDER_INVITE.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_USER.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId(),
                NotificationType.UPLOAD_SUCCESS.getTypeId(),
                NotificationType.UPLOAD_FAILED.getTypeId(),
                NotificationType.INQUIRY.getTypeId()
        };

        Assertions
                .assertThat(response.notifications())
                .hasSize(8)
                .extracting("type")
                .containsOnly(Stream.of(types).map(Object::toString).toArray());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공지사항)")
    @WithAccount
    void getNotifications_Success_Notice() throws Exception {
        String type = NotificationSort.NOTICE.getValue();
        int page = 1,
                count = 10;

        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);
        log.info("type = {}", response.notifications().stream().map(
                NotificationDto::getType
        ).toList());

        Assertions
                .assertThat(response.total())
                .isEqualTo(1);

        Assertions
                .assertThat(response.notifications())
                .hasSize(1)
                .extracting("type")
                .containsOnly(String.valueOf(NotificationType.NOTICE.getTypeId()));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공유)")
    @WithAccount
    void getNotifications_Success_Share() throws Exception {
        String type = NotificationSort.SHARE.getValue();
        int page = 1,
                count = 10;

        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);
        log.info("type = {}", response.notifications().stream().map(
                NotificationDto::getType
        ).toList());

        Assertions
                .assertThat(response.total())
                .isEqualTo(4);

        Character[] types = {
                NotificationType.SHARE_FOLDER_INVITE.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_USER.getTypeId(),
                NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId()
        };

        Assertions
                .assertThat(response.notifications())
                .hasSize(4)
                .extracting("type")
                .containsOnly(Stream.of(types).map(Object::toString).toArray());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (문서)")
    @WithAccount
    void getNotifications_Success_Record() throws Exception {
        String type = NotificationSort.RECORD.getValue();
        int page = 1,
                count = 10;

        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);
        log.info("type = {}", response.notifications().stream().map(
                NotificationDto::getType
        ).toList());

        Assertions
                .assertThat(response.total())
                .isEqualTo(2);

        Character[] types = {
                NotificationType.UPLOAD_SUCCESS.getTypeId(),
                NotificationType.UPLOAD_FAILED.getTypeId()
        };

        Assertions
                .assertThat(response.notifications())
                .hasSize(2)
                .extracting("type")
                .containsOnly(Stream.of(types).map(Object::toString).toArray());
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (문의)")
    @WithAccount
    void getNotifications_Success_Inquiry() throws Exception {
        String type = NotificationSort.INQUIRY.getValue();
        int page = 1,
                count = 10;

        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);
        log.info("type = {}", response.notifications().stream().map(
                NotificationDto::getType
        ).toList());

        Assertions
                .assertThat(response.total())
                .isEqualTo(1);

        Assertions
                .assertThat(response.notifications())
                .hasSize(1)
                .extracting("type")
                .containsOnly(String.valueOf(NotificationType.INQUIRY.getTypeId()));
    }

    @Test
    @DisplayName("안 읽은 알림 여부 조회 - 성공 (존재)")
    @WithAccount
    void hasUnreadNotifications_Success_Exists() throws Exception {
        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadNotificationDto hasUnread = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadNotificationDto.class
        );

        log.info("hasUnread = {}", hasUnread);

        Assertions
                .assertThat(hasUnread.hasUnRead())
                .isTrue();
    }

    @Test
    @DisplayName("안 읽은 알림 여부 조회 - 성공 (알림 X)")
    @WithAccount
    void hasUnreadNotifications_Success_NotExists() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadNotificationDto hasUnread = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadNotificationDto.class
        );

        log.info("hasUnread = {}", hasUnread);

        Assertions
                .assertThat(hasUnread.hasUnRead())
                .isFalse();
    }

    @Test
    @DisplayName("안 읽은 알림 여부 조회 - 성공 (읽음)")
    @WithAccount
    void hasUnreadNotifications_Success_Read() throws Exception {
        NotificationEntity notification = notificationFixture.save(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                        .build()
        );

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(notification)
                        .user(myUser)
                        .isDeleted(false)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadNotificationDto hasUnread = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadNotificationDto.class
        );

        log.info("hasUnread = {}", hasUnread);

        Assertions
                .assertThat(hasUnread.hasUnRead())
                .isFalse();
    }

    @Test
    @DisplayName("안 읽음 알림 개수 조회 - 성공 (존재)")
    @WithAccount
    void getUnreadNotificationCount_Success_Exists() throws Exception {
        createAllNotifications();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/count")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadCountDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadCountDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.all())
                .isEqualTo(8);

        Assertions
                .assertThat(response.notice())
                .isEqualTo(1);

        Assertions
                .assertThat(response.share())
                .isEqualTo(4);

        Assertions
                .assertThat(response.document())
                .isEqualTo(2);

        Assertions
                .assertThat(response.inquiry())
                .isEqualTo(1);
    }

    @Test
    @DisplayName("안 읽음 알림 개수 조회 - 성공 (알림 X)")
    @WithAccount
    void getUnreadNotificationCount_Success_NotExists() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/count")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadCountDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadCountDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.all())
                .isEqualTo(0);

        Assertions
                .assertThat(response.notice())
                .isEqualTo(0);

        Assertions
                .assertThat(response.share())
                .isEqualTo(0);

        Assertions
                .assertThat(response.document())
                .isEqualTo(0);

        Assertions
                .assertThat(response.inquiry())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("안 읽음 알림 개수 조회 - 성공 (읽음)")
    @WithAccount
    void getUnreadNotificationCount_Success_Read() throws Exception {
        NotificationEntity notification = notificationFixture.save(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                        .build()
        );

        notificationReadFixture.save(
                NotificationReadFixture.builder()
                        .notification(notification)
                        .user(myUser)
                        .isDeleted(false)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/count")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadCountDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadCountDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.all())
                .isEqualTo(0);

        Assertions
                .assertThat(response.notice())
                .isEqualTo(0);

        Assertions
                .assertThat(response.share())
                .isEqualTo(0);

        Assertions
                .assertThat(response.document())
                .isEqualTo(0);

        Assertions
                .assertThat(response.inquiry())
                .isEqualTo(0);
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    @WithAccount
    void markNotificationAsRead_Success() throws Exception {
        NotificationEntity notification = notificationFixture.save(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/notification/{notificationId}", notification.getNotificationId())
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 알림 읽음 처리 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadNotificationDto hasUnread = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadNotificationDto.class
        );

        log.info("hasUnread = {}", hasUnread);

        Assertions
                .assertThat(hasUnread.hasUnRead())
                .isFalse();
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공 (중복)")
    @WithAccount
    void markNotificationAsRead_Success_Duplicate() throws Exception {
        NotificationEntity notification = notificationFixture.save(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/notification/{notificationId}", notification.getNotificationId())
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 중복으로 읽음 처리
        mockMvc.perform(
                        MockMvcRequestBuilders.post("/notification/{notificationId}", notification.getNotificationId())
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 알림 읽음 처리 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUnReadNotificationDto hasUnread = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseUnReadNotificationDto.class
        );

        log.info("hasUnread = {}", hasUnread);

        Assertions
                .assertThat(hasUnread.hasUnRead())
                .isFalse();
    }

    @Test
    @DisplayName("알림 읽음 처리 - 실패 (존재하지 않는 알림)")
    @WithAccount
    void markNotificationAsRead_Failure_NotFound() throws Exception {
        Long nonExistentNotificationId = 999L; // 존재하지 않는 알림 ID

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/notification/{notificationId}", nonExistentNotificationId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_NOTIFICATION.getErrorCode()));
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    @WithAccount
    void deleteNotification_Success() throws Exception {
        NotificationEntity notification = notificationFixture.save(
                NotificationFixture.builder()
                        .type(NotificationType.NOTICE)
                        .sender(myUser)
                        .notice(notice)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/notification/{notificationId}", notification.getNotificationId())
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 알림 삭제 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNotificationDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNotificationDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(0);

        Assertions
                .assertThat(response.notifications())
                .isEmpty();
    }
}

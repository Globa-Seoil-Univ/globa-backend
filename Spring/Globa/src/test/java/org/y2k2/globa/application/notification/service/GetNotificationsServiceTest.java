package org.y2k2.globa.application.notification.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.notification.dto.common.NotificationParameters;
import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
import org.y2k2.globa.common.type.NotificationSort;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjection;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationProjectionImpl;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class GetNotificationsServiceTest {
    @InjectMocks
    private GetNotificationsService getNotificationsService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공지)")
    void getNotifications_Success_Notice() {
        int page = 1,
                count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        NotificationSort sort = NotificationSort.NOTICE;
        List<NotificationProjectionImpl> notificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.NOTICE.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("noticeId", Arbitraries.longs().greaterOrEqual(1))
                .set("noticeTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("noticeContent", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("noticeThumbnail", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(8); // 공지 알림 8개 생성

        // Impl을 사용하여 NotificationProjection의 구현체를 생성
        List<NotificationProjection> notifications =  notificationProjections.stream()
                .map(notificationProjection -> (NotificationProjection) notificationProjection)
                .collect(Collectors.toList());

        Mockito
                .when(notificationRepository.getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(notifications, pageable, notifications.size()));

        ResponseNotificationDto response = getNotificationsService.get(count, page, sort, userId);

        log.info("Response = {}", response);

        Assertions
                .assertThat(response.notifications())
                .hasSize(8)
                .allSatisfy(notification -> {
                    Assertions
                            .assertThat(notification.getType())
                            .isEqualTo(String.valueOf(NotificationType.NOTICE.getTypeId()));
                });

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (공유)")
    void getNotifications_Success_Share() {
        int page = 1,
                count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        NotificationSort sort = NotificationSort.SHARE;
        List<NotificationProjectionImpl> addFileNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_FILE.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("recordId", Arbitraries.longs().greaterOrEqual(1))
                .set("recordTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("userName", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("userProfile", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 공유 파일 알림 3개 생성

        List<NotificationProjectionImpl> addShareUserNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_USER.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("userName", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("userProfile", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 공유 사용자 알림 3개 생성

        List<NotificationProjectionImpl> addCommentNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.SHARE_FOLDER_ADD_COMMENT.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("recordId", Arbitraries.longs().greaterOrEqual(1))
                .set("recordTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("commentId", Arbitraries.longs().greaterOrEqual(1))
                .set("commentContent", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("userName", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("userProfile", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(200))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 공유 댓글 알림 3개 생성

        // Impl을 사용하여 NotificationProjection의 구현체를 생성
        List<NotificationProjection> notifications = Stream.concat(
                addFileNotificationProjections.stream(),
                Stream.concat(
                        addShareUserNotificationProjections.stream(),
                        addCommentNotificationProjections.stream()
                )
        ).collect(Collectors.toList());

        Mockito
                .when(notificationRepository.getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(notifications, pageable, notifications.size()));

        ResponseNotificationDto response = getNotificationsService.get(count, page, sort, userId);

        log.info("Response = {}", response);

        Assertions
                .assertThat(response.notifications())
                .hasSize(9)
                .allSatisfy(notification -> {
                    Assertions.assertThat(notification.getType()).isIn(
                            String.valueOf(NotificationType.SHARE_FOLDER_ADD_FILE.getTypeId()),
                            String.valueOf(NotificationType.SHARE_FOLDER_ADD_USER.getTypeId()),
                            String.valueOf(NotificationType.SHARE_FOLDER_ADD_COMMENT.getTypeId())
                    );
                });

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (업로드 알림)")
    void getNotifications_Success_Upload() {
        int page = 1,
                count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        NotificationSort sort = NotificationSort.RECORD;
        List<NotificationProjectionImpl> uploadSuccessNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.UPLOAD_SUCCESS.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("recordId", Arbitraries.longs().greaterOrEqual(1))
                .set("recordTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 업로드 성공 알림 3개 생성

        List<NotificationProjectionImpl> uploadFailedNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.UPLOAD_FAILED.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderId", Arbitraries.longs().greaterOrEqual(1))
                .set("folderTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("recordId", Arbitraries.longs().greaterOrEqual(1))
                .set("recordTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 업로드 실패 알림 3개 생성

        // Impl을 사용하여 NotificationProjection의 구현체를 생성
        List<NotificationProjection> notifications = Stream.concat(
                        uploadSuccessNotificationProjections.stream(),
                        uploadFailedNotificationProjections.stream()
                )
                .collect(Collectors.toList());

        Mockito
                .when(notificationRepository.getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(notifications, pageable, notifications.size()));

        ResponseNotificationDto response = getNotificationsService.get(count, page, sort, userId);

        log.info("Response = {}", response);

        Assertions
                .assertThat(response.notifications())
                .hasSize(6)
                .allSatisfy(notification -> {
                    Assertions.assertThat(notification.getType()).isIn(
                            String.valueOf(NotificationType.UPLOAD_SUCCESS.getTypeId()),
                            String.valueOf(NotificationType.UPLOAD_FAILED.getTypeId())
                    );
                });

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (문의)")
    void getNotifications_Success_Inquiry() {
        int page = 1,
                count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        NotificationSort sort = NotificationSort.INQUIRY;
        List<NotificationProjectionImpl> inquiryNotificationProjections = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build()
                .giveMeBuilder(NotificationProjectionImpl.class)
                .set("type", NotificationType.INQUIRY.name())
                .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                .set("inquiryId", Arbitraries.longs().greaterOrEqual(1))
                .set("inquiryTitle", Arbitraries.strings().alpha().ofMinLength(1).ofMaxLength(60))
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sampleList(3); // 문의 알림 3개 생성

        // Impl을 사용하여 NotificationProjection의 구현체를 생성
        List<NotificationProjection> notifications = inquiryNotificationProjections.stream()
                .map(notificationProjection -> (NotificationProjection) notificationProjection)
                .collect(Collectors.toList());

        Mockito
                .when(notificationRepository.getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(notifications, pageable, notifications.size()));

        ResponseNotificationDto response = getNotificationsService.get(count, page, sort, userId);

        log.info("Response = {}", response);

        Assertions
                .assertThat(response.notifications())
                .hasSize(3)
                .allSatisfy(notification -> {
                    Assertions
                            .assertThat(notification.getType())
                            .isEqualTo(String.valueOf(NotificationType.INQUIRY.getTypeId()));
                });

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable));
    }

    @Test
    @DisplayName("알림 목록 조회 - 성공 (모든 알림)")
    void getNotifications_Success_All() {
        int page = 1,
                count = 10;
        Long userId = 1L;
        Pageable pageable = PageRequest.of(page - 1, count);

        NotificationSort sort = NotificationSort.ALL;
        List<NotificationProjectionImpl> allNotificationProjections = new ArrayList<>();

        // 각 알림 타입별로 하나씩 생성
        NotificationType[] types = {
                NotificationType.NOTICE,
                NotificationType.SHARE_FOLDER_ADD_FILE,
                NotificationType.SHARE_FOLDER_ADD_USER,
                NotificationType.SHARE_FOLDER_ADD_COMMENT,
                NotificationType.UPLOAD_SUCCESS,
                NotificationType.UPLOAD_FAILED,
                NotificationType.INQUIRY
        };

        FixtureMonkey fixtureMonkey = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(false)
                .build();

        for (NotificationType type : types) {
            NotificationProjectionImpl notification = fixtureMonkey
                    .giveMeBuilder(NotificationProjectionImpl.class)
                    .set("notificationId", Arbitraries.longs().greaterOrEqual(1))
                    .set("type", type.name())
                    .set("createdTime", new CustomTimestamp().getTimestamp())
                    // 각 알림 타입에 맞는 필드를 설정은 생략
                    .sample();

            allNotificationProjections.add(notification);
        }

        // Impl을 사용하여 NotificationProjection의 구현체를 생성
        List<NotificationProjection> notifications = allNotificationProjections.stream()
                .map(notificationProjection -> (NotificationProjection) notificationProjection)
                .collect(Collectors.toList());

        Mockito
                .when(notificationRepository.getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable)))
                .thenReturn(new PageImpl<>(notifications, pageable, notifications.size()));

        ResponseNotificationDto response = getNotificationsService.get(count, page, sort, userId);

        log.info("Response = {}", response);

        Assertions
                .assertThat(response.notifications())
                .hasSize(7);

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getNotifications(Mockito.eq(userId), Mockito.any(NotificationParameters.class), Mockito.eq(pageable));
    }
}

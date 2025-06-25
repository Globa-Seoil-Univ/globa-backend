package org.y2k2.globa.application.notification.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadCountDto;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;
import org.y2k2.globa.infrastructure.persistence.notification.projection.NotificationUnReadCountProjectionImpl;

@ExtendWith(MockitoExtension.class)
public class GetUnReadCountNotificationServiceTest {
    @InjectMocks
    private GetUnReadCountNotificationService getUnReadCountNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("읽지 않은 알림 조회 - 성공")
    public void getUnReadCount_Success() {
        Long userId = 1L;
        Long noticeCount = 5L,
                inviteCount = 3L,
                shareCount = 2L,
                recordCount = 4L,
                inquiryCount = 1L;

        NotificationUnReadCountProjectionImpl notificationUnReadCountProjection = new NotificationUnReadCountProjectionImpl(
                noticeCount,
                inviteCount,
                shareCount,
                recordCount,
                inquiryCount
        );

        Mockito
                .when(notificationRepository.getUnReadCount(userId))
                .thenReturn(notificationUnReadCountProjection);

        ResponseUnReadCountDto response = getUnReadCountNotificationService.get(userId);

        Assertions
                .assertThat(response.all())
                .isEqualTo(noticeCount + inviteCount + shareCount + recordCount + inquiryCount);

        Assertions
                .assertThat(response.notice())
                .isEqualTo(noticeCount);

        Assertions
                .assertThat(response.share())
                .isEqualTo(inviteCount + shareCount);

        Assertions
                .assertThat(response.document())
                .isEqualTo(recordCount);

        Assertions
                .assertThat(response.inquiry())
                .isEqualTo(inquiryCount);

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getUnReadCount(userId);
    }

    @Test
    @DisplayName("읽지 않은 알림 조회 - 성공 (안 읽은 알림 X)")
    public void getUnReadCount_NoUnReadNotifications() {
        Long userId = 1L;

        NotificationUnReadCountProjectionImpl notificationUnReadCountProjection = new NotificationUnReadCountProjectionImpl(
                0L, 0L, 0L, 0L, 0L
        );

        Mockito
                .when(notificationRepository.getUnReadCount(userId))
                .thenReturn(notificationUnReadCountProjection);

        ResponseUnReadCountDto response = getUnReadCountNotificationService.get(userId);

        Assertions
                .assertThat(response.all())
                .isEqualTo(0L);

        Assertions
                .assertThat(response.notice())
                .isEqualTo(0L);

        Assertions
                .assertThat(response.share())
                .isEqualTo(0L);

        Assertions
                .assertThat(response.document())
                .isEqualTo(0L);

        Assertions
                .assertThat(response.inquiry())
                .isEqualTo(0L);

        Mockito
                .verify(notificationRepository, Mockito.times(1))
                .getUnReadCount(userId);
    }
}

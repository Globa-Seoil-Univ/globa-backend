package org.y2k2.globa.application.notification.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadNotificationDto;
import org.y2k2.globa.domain.notification.repository.NotificationRepository;

@ExtendWith(MockitoExtension.class)
public class HasUnReadNotificationServiceTest {
    @InjectMocks
    private HasUnReadNotificationService hasUnReadNotificationService;

    @Mock
    private NotificationRepository notificationRepository;

    @Test
    @DisplayName("읽지 않은 알림 여부 조회 - 성공 (존재 O)")
    public void hasUnReadNotification_Success() {
        Long userId = 1L;

        Mockito
                .when(notificationRepository.hasUnReadNotification(userId))
                .thenReturn(true);

        ResponseUnReadNotificationDto response = hasUnReadNotificationService.get(userId);

        Assertions
                .assertThat(response.getHasUnRead())
                .isTrue();
    }

    @Test
    @DisplayName("읽지 않은 알림 여부 조회 - 성공 (존재 X)")
    public void hasUnReadNotification_Failure() {
        Long userId = 1L;

        Mockito
                .when(notificationRepository.hasUnReadNotification(userId))
                .thenReturn(false);

        ResponseUnReadNotificationDto response = hasUnReadNotificationService.get(userId);

        Assertions
                .assertThat(response.getHasUnRead())
                .isFalse();
    }
}

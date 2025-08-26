package org.y2k2.globa.api.notification;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.NotificationController;
import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadCountDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadNotificationDto;
import org.y2k2.globa.application.notification.service.*;
import org.y2k2.globa.common.type.NotificationSort;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = NotificationController.class)
public class NotificationControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetNotificationsService getNotificationsService;
    @MockBean
    private HasUnReadNotificationService hasUnReadNotificationService;
    @MockBean
    private GetUnReadCountNotificationService getUnReadCountNotificationService;
    @MockBean
    private ReadNotificationService readNotificationService;
    @MockBean
    private DeleteNotificationService deleteNotificationService;

    @Test
    @DisplayName("알림 목록 조회 - 성공")
    @WithAccount
    void getNotifications_Success() throws Exception {
        String type = NotificationSort.ALL.getValue();
        int page = 1,
                count = 10;

        ResponseNotificationDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseNotificationDto.class);

        Mockito
                .when(getNotificationsService.get(
                        Mockito.eq(page),
                        Mockito.eq(count),
                        Mockito.any(NotificationSort.class),
                        Mockito.anyLong()
                ))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                                .param("type", type)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("안 읽은 알림 여부 조회 - 성공")
    @WithAccount
    void hasUnReadNotification_Success() throws Exception {
        ResponseUnReadNotificationDto response = new ResponseUnReadNotificationDto(true);

        Mockito
                .when(hasUnReadNotificationService.get(Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/check")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("안 읽은 알림 개수 조회 - 성공")
    @WithAccount
    void getUnReadCountNotification_Success() throws Exception {
        ResponseUnReadCountDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseUnReadCountDto.class)
                .set("all", 8L)
                .set("notice", 1L)
                .set("share", 4L)
                .set("document", 2L)
                .set("inquiry", 1L)
                .sample();

        Mockito
                .when(getUnReadCountNotificationService.get(Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/notification/unread/count")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("알림 읽음 처리 - 성공")
    @WithAccount
    void readNotification_Success() throws Exception {
        Long notificationId = 1L;

        Mockito
                .doNothing()
                .when(readNotificationService)
                .read(Mockito.anyLong(), Mockito.anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/notification/{notificationId}", notificationId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("알림 삭제 - 성공")
    @WithAccount
    void deleteNotification_Success() throws Exception {
        Long notificationId = 1L;

        Mockito
                .doNothing()
                .when(deleteNotificationService)
                .delete(Mockito.anyLong(), Mockito.anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/notification/{notificationId}", notificationId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}

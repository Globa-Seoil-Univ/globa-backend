package org.y2k2.globa.api.fcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
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
import org.y2k2.globa.api.FcmController;
import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.application.fcm.service.PushMessageService;
import org.y2k2.globa.application.fcm.service.SubscribeTopicService;
import org.y2k2.globa.application.fcm.service.UnSubscribeTopicService;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = FcmController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FcmControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PushMessageService pushMessageService;
    @MockBean
    private SubscribeTopicService subscribeTopicService;
    @MockBean
    private UnSubscribeTopicService unSubscribeTopicService;

    @Test
    @DisplayName("FCM 토픽 전송 - 성공")
    @WithAccount
    void pushMessage_Success() throws Exception {
        RequestFcmTopicDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        Mockito
                .doNothing()
                .when(pushMessageService)
                .send(Mockito.any(RequestFcmTopicDto.class), Mockito.anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito
                .verify(pushMessageService, Mockito.times(1))
                .send(Mockito.any(RequestFcmTopicDto.class), Mockito.anyLong());
    }

    @Test
    @DisplayName("FCM 토픽 전송 - 실패 (잘못된 요청)")
    @WithAccount
    void pushMessage_Fail_BadRequest() throws Exception {
        RequestFcmTopicDto request = new RequestFcmTopicDto(
                null,
                null,
                null
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("FCM 토픽 가입 - 성공")
    @WithAccount
    void subscribeTopic_Success() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        Mockito
                .doNothing()
                .when(subscribeTopicService)
                .subscribe(Mockito.any(RequestSubscribeTopicDto.class), Mockito.anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito
                .verify(subscribeTopicService, Mockito.times(1))
                .subscribe(Mockito.any(RequestSubscribeTopicDto.class), Mockito.anyLong());
    }

    @Test
    @DisplayName("FCM 토픽 가입 - 실패 (알 수 없는 토픽)")
    @WithAccount
    void subscribeTopic_Fail_UnknownTopic() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                "unknown-topic"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("FCM 토픽 가입 - 실패 (잘못된 요청)")
    @WithAccount
    void subscribeTopic_Fail_BadRequest() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                null
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("FCM 토픽 탈퇴 - 성공")
    @WithAccount
    void unsubscribeTopic_Success() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        Mockito
                .doNothing()
                .when(unSubscribeTopicService)
                .unsubscribe(Mockito.any(RequestSubscribeTopicDto.class), Mockito.anyLong());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito
                .verify(unSubscribeTopicService, Mockito.times(1))
                .unsubscribe(Mockito.any(RequestSubscribeTopicDto.class), Mockito.anyLong());
    }

    @Test
    @DisplayName("FCM 토픽 탈퇴 - 실패 (알 수 없는 토픽)")
    @WithAccount
    void unsubscribeTopic_Fail_UnknownTopic() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                "unknown-topic"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("FCM 토픽 탈퇴 - 실패 (잘못된 요청)")
    @WithAccount
    void unsubscribeTopic_Fail_BadRequest() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                null
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

package org.y2k2.globa.api.fcm;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class FcmIntegrationTest extends IntegrationTest {
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

    private RoleEntity editor;
    private RoleEntity viewer;
    private RoleEntity publicUser;

    @BeforeEach
    void setup() {
        RoleEntity admin = roleFixture.getEntity(UserRole.ADMIN);
        editor = roleFixture.getEntity(UserRole.EDITOR);
        viewer = roleFixture.getEntity(UserRole.VIEWER);
        publicUser = roleFixture.getEntity(UserRole.USER);

        UserEntity myUser = userFixture.save(
                UserFixture.builder()
                        .name("Test User")
                        .build()
        );

        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(admin)
                        .build()
        );

        setSecurityContext(myUser);
    }

    @Test
    @DisplayName("FCM 토픽 전송 - 성공 (ADMIN)")
    @WithAccount
    void pushMessage_Success() throws Exception {
        RequestFcmTopicDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("FCM 토픽 전송 - 성공 (EDITOR)")
    @WithAccount
    void pushMessage_Success_Editor() throws Exception {
        UserEntity editorUser = userFixture.save(
                UserFixture.builder()
                        .name("Editor User")
                        .build()
        );
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(editorUser)
                        .role(editor) // EDITOR 권한 부여
                        .build()
        );

        setSecurityContext(editorUser);

        RequestFcmTopicDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("FCM 토픽 전송 - 실패 (VIEWER)")
    @WithAccount
    void pushMessage_Fail_Forbidden() throws Exception {
        UserEntity viewerUser = userFixture.save(
                UserFixture.builder()
                        .name("Other User")
                        .build()
        );
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(viewerUser)
                        .role(viewer) // 다른 사용자에게는 VIEWER 권한만 부여
                        .build()
        );

        setSecurityContext(viewerUser);

        RequestFcmTopicDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("FCM 토픽 전송 - 실패 (PUBLIC_USER)")
    @WithAccount
    void pushMessage_Fail_PublicUser() throws Exception {
        UserEntity publicUserEntity = userFixture.save(
                UserFixture.builder()
                        .name("Public User")
                        .build()
        );
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(publicUserEntity)
                        .role(publicUser) // PUBLIC_USER 권한 부여
                        .build()
        );

        setSecurityContext(publicUserEntity);

        RequestFcmTopicDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFcmTopicDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/send")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("FCM 토픽 가입 - 성공")
    @WithAccount
    void subscribeTopic_Success() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("FCM 토픽 가입 - 실패 (FCM 토큰 없음)")
    @WithAccount
    void subscribeTopic_Fail_NoNotificationToken() throws Exception {
        // FCM 토큰이 없는 사용자 설정
        UserEntity userWithoutToken = userFixture.save(
                UserFixture.builder()
                        .name("User Without Token")
                        .build()
        );
        userWithoutToken.setNotificationToken(null);
        userFixture.save(userWithoutToken);

        setSecurityContext(userWithoutToken);

        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN.getErrorCode()));
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }

    @Test
    @DisplayName("FCM 토픽 탈퇴 - 성공")
    @WithAccount
    void unsubscribeTopic_Success() throws Exception {
        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("FCM 토픽 탈퇴 - 실패 (FCM 토큰 없음)")
    @WithAccount
    void unsubscribeTopic_Fail_NoNotificationToken() throws Exception {
        // FCM 토큰이 없는 사용자 설정
        UserEntity userWithoutToken = userFixture.save(
                UserFixture.builder()
                        .name("User Without Token")
                        .build()
        );
        userWithoutToken.setNotificationToken(null);
        userFixture.save(userWithoutToken);

        setSecurityContext(userWithoutToken);

        RequestSubscribeTopicDto request = new RequestSubscribeTopicDto(
                FcmTopic.NOTICE.getTopic()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/fcm/topic")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN.getErrorCode()));
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
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").exists());
    }
}

package org.y2k2.globa.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.bcel.Const;
import org.aspectj.lang.annotation.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.UserController;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.user.dto.request.*;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.service.*;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;


@Slf4j
@Import(ControllerConfig.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetUserService getUserService;
    @MockBean
    private GetSearchUserService getSearchUserService;
    @MockBean
    private GetUserNotificationService getUserNotificationService;
    @MockBean
    private GetUserAnalysisService getUserAnalysisService;
    @MockBean
    private CreateUserService createUserService;
    @MockBean
    private ReissueTokenService reissueTokenService;
    @MockBean
    private UpsertFcmService upsertFcmService;
    @MockBean
    private UpdateUserNameService updateUserNameService;
    @MockBean
    private UpdateUserProfileImgService updateUserProfileImgService;
    @MockBean
    private UpdateUserNotificationService updateUserNotificationService;
    @MockBean
    private DeleteUserService deleteUserService;

    @Test
    @DisplayName("내 정보 가져오기 성공")
    @WithAccount
    void getMyInfoTest() throws Exception {
        ResponseUserDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseUserDto.class);

        Mockito.when(getUserService.getUser(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        log.info("jwt: {}", jwt.getGrantType() + jwt.getAccessToken());

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue()  )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getUserService, Mockito.times(1)).getUser(ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("정보 가져오기 성공")
    @WithAccount
    void searchUserTest() throws Exception {
        ResponseUserSearchDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseUserSearchDto.class);

        Mockito.when(getSearchUserService.getSearchUser(ArgumentMatchers.any(String.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("code", "ABCDEF")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getSearchUserService, Mockito.times(1))
                .getSearchUser(ArgumentMatchers.any(String.class));
    }

    @Test
    @DisplayName("내 알림 정보 가져오기 성공")
    @WithAccount
    void getMyNotificationTest() throws Exception {
        ResponseNotificationSettingDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseNotificationSettingDto.class);

        Mockito.when(getUserNotificationService.getUserNotification(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/notification")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getUserNotificationService, Mockito.times(1))
                .getUserNotification(ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("내 분석 정보 가져오기 성공")
    @WithAccount
    void getMyAnalysisTest() throws Exception {
        ResponseAnalysisDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseAnalysisDto.class);

        Mockito.when(getUserAnalysisService.getAnalysis(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getUserAnalysisService, Mockito.times(1))
                .getAnalysis(ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("회원가입과 로그인 성공")
    void signupOrLoginTest() throws Exception {
        RequestUserPostDTO request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestUserPostDTO.class)
                .set("snsKind", "KAKAO")
                .set("snsId", "ASDASD")
                .set("name", "test_name")
                .set("token", "fcm_token")
                .sample();

        Mockito.when(createUserService.signupOrLogin(ArgumentMatchers.any(RequestUserPostDTO.class)))
                .thenReturn(jwt);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(createUserService, Mockito.times(1))
                .signupOrLogin(ArgumentMatchers.any(RequestUserPostDTO.class));
    }

    @Test
    @DisplayName("Access Token 갱신")
    void reissueTokenTest() throws Exception {
        RequestRTRDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestRTRDto.class);

        Mockito.when(reissueTokenService.reissue(
                        ArgumentMatchers.any(String.class),
                        ArgumentMatchers.any(String.class)
                ))
                .thenReturn(jwt);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/refresh")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(reissueTokenService, Mockito.times(1))
                .reissue(ArgumentMatchers.any(String.class), ArgumentMatchers.any(String.class));
    }

    @Test
    @DisplayName("FCM 토큰 저장 또는 수정 성공")
    @WithAccount
    void upsertFcmTokenTest() throws Exception {
        RequestNotificationTokenDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestNotificationTokenDto.class);

        Mockito.doNothing()
                .when(upsertFcmService)
                .upsert(
                        ArgumentMatchers.any(RequestNotificationTokenDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/notification/token")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(upsertFcmService, Mockito.times(1))
                .upsert(ArgumentMatchers.any(RequestNotificationTokenDto.class), ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("알림 정보 수정 성공")
    @WithAccount
    void modifyNotificationTest() throws Exception {
        RequestNotificationSettingDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestNotificationSettingDto.class);

        Mockito.doNothing()
                .when(updateUserNotificationService)
                .update(
                        ArgumentMatchers.any(RequestNotificationSettingDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.put(Constant.USER_PREFIX.getValue() + "/notification")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateUserNotificationService, Mockito.times(1))
                .update(ArgumentMatchers.any(RequestNotificationSettingDto.class), ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("이름 수정 성공")
    @WithAccount
    void modifyUserNameTest() throws Exception {
        RequestNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestNameDto.class);

        Mockito.doNothing()
                .when(updateUserNameService)
                .update(
                        ArgumentMatchers.any(RequestNameDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(Constant.USER_PREFIX.getValue() + "/name")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateUserNameService, Mockito.times(1))
                .update(ArgumentMatchers.any(RequestNameDto.class), ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("프로필 이미지 수정 성공")
    @WithAccount
    void modifyUserProfileImgTest() throws Exception {
        RequestProfileImageDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestProfileImageDto.class)
                .set(
                        "profile",
                        new MockMultipartFile(
                                "profile",
                                "profile.jpg",
                                "image/jpeg",
                                "testdata".getBytes()
                        )
                )
                .sample();

        Mockito.doNothing()
                .when(updateUserProfileImgService)
                .update(
                        ArgumentMatchers.any(RequestProfileImageDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(Constant.USER_PREFIX.getValue() + "/profile")
                                .file((MockMultipartFile) request.profile())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(req -> {
                                    req.setMethod("PATCH");
                                    return req;
                                })
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateUserProfileImgService, Mockito.times(1))
                .update(ArgumentMatchers.any(RequestProfileImageDto.class), ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("프로필 이미지 수정 실패 (비어있는 파일)")
    @WithAccount
    void failedModifyUserProfileImgTest() throws Exception {
        RequestProfileImageDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestProfileImageDto.class)
                .set(
                        "profile",
                        new MockMultipartFile(
                                "profile",
                                "profile.jpg",
                                "image/jpeg",
                                new byte[0]
                        )
                )
                .sample();

        Mockito.doNothing()
                .when(updateUserProfileImgService)
                .update(
                        ArgumentMatchers.any(RequestProfileImageDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.multipart(Constant.USER_PREFIX.getValue() + "/profile")
                                .file((MockMultipartFile) request.profile())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                                .with(req -> {
                                    req.setMethod("PATCH");
                                    return req;
                                })
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(updateUserProfileImgService, Mockito.times(0))
                .update(ArgumentMatchers.any(RequestProfileImageDto.class), ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("회원 탈퇴 성공")
    @WithAccount
    void deleteUserTest() throws Exception {
        RequestSurveyDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestSurveyDto.class)
                .set("surveyType", "BSV")
                .set("content", "서비스 사용이 너무 어려워요.")
                .sample();

        Mockito.doNothing()
                .when(deleteUserService)
                .delete(
                        ArgumentMatchers.any(RequestSurveyDto.class),
                        ArgumentMatchers.any(Long.class)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.USER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(deleteUserService, Mockito.times(1))
                .delete(ArgumentMatchers.any(RequestSurveyDto.class), ArgumentMatchers.any(Long.class));
    }
}

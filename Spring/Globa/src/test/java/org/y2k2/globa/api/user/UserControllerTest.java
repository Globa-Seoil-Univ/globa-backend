package org.y2k2.globa.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.instantiator.Instantiator;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.UserController;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.service.*;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;


@Slf4j
@Import(ControllerConfig.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

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

    private final String prefix = "/user";

    @Test
    @DisplayName("내 정보 가져오기 성공")
    @WithAccount
    void getMyInfoTest() throws Exception {
        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(JWT.class);

        ResponseUserDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseUserDto.class);

        Mockito.when(getUserService.getUser(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(prefix)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getUserService, Mockito.times(1)).getUser(ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("유저 정보 가져오기 성공")
    @WithAccount
    void searchUserTest() throws Exception {
        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(JWT.class);

        ResponseUserSearchDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseUserSearchDto.class);

        Mockito.when(getSearchUserService.getSearchUser(ArgumentMatchers.any(String.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(prefix + "/search")
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
        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(JWT.class);

        ResponseNotificationSettingDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseNotificationSettingDto.class);

        Mockito.when(getUserNotificationService.getUserNotification(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(prefix + "/notification")
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
        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(JWT.class);

        ResponseAnalysisDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseAnalysisDto.class);

        Mockito.when(getUserAnalysisService.getAnalysis(ArgumentMatchers.any(Long.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(prefix + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getUserAnalysisService, Mockito.times(1))
                .getAnalysis(ArgumentMatchers.any(Long.class));
    }

    @Test
    @DisplayName("회원가입과 로그인 성공")
    public void signupOrLoginTest() throws Exception {
        RequestUserPostDTO request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestUserPostDTO.class)
                .set("snsKind", "KAKAO")
                .set("snsId", "ASDASD")
                .set("name", "test_name")
                .set("token", "fcm_token")
                .sample();

        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(JWT.class);

        Mockito.when(createUserService.signupOrLogin(ArgumentMatchers.any(RequestUserPostDTO.class)))
                .thenReturn(jwt);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(prefix)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(createUserService, Mockito.times(1))
                .signupOrLogin(ArgumentMatchers.any(RequestUserPostDTO.class));
    }
}

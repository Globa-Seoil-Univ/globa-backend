package org.y2k2.globa.api.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
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
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.service.*;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;


@Slf4j
@Import(ControllerConfig.class)
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

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
    void testGetMyInfo() throws Exception {
        JWT jwt = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
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

//    @Test
//    @DisplayName("유저 정보 가져오기 성공")
//    @WithAccount
//    void testGetUser() throws Exception {
//        JWT jwt = JWTHelper.createJWT();
//        UserEntity user = UserHelper.createUser();
//        ResponseUserSearchDto response = UserHelper.createResponseUserSearchDto(user);
//
//        Mockito.when(userService.searchUser(ArgumentMatchers.any(String.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(
//                        MockMvcRequestBuilders.get(prefix + "/search")
//                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
//                                .param("code", "ABCDEF")
//                                .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(MockMvcResultMatchers.status().isOk());
//
//        Mockito.verify(userService, Mockito.times(1)).searchUser(ArgumentMatchers.any(String.class));
//    }
//
//    @Test
//    @DisplayName("내 알림 정보 가져오기 성공")
//    @WithAccount
//    void testGetMyNotification() throws Exception {
//        JWT jwt = JWTHelper.createJWT();
//        UserEntity user = UserHelper.createUser();
//        ResponseNotificationSettingDto response = UserHelper.createResponseNotificationSettingDto(user);
//
//        Mockito.when(userService.getNotification(ArgumentMatchers.any(UserEntity.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(
//                        MockMvcRequestBuilders.get(prefix + "/notification")
//                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
//                                .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(MockMvcResultMatchers.status().isOk());
//
//        Mockito.verify(userService, Mockito.times(1)).getNotification(ArgumentMatchers.any(UserEntity.class));
//    }
//
//    @Test
//    @DisplayName("내 분석 정보 가져오기 성공")
//    @WithAccount
//    void getMyAnalysis() throws Exception {
//        JWT jwt = JWTHelper.createJWT();
//        UserEntity user = UserHelper.createUser();
//        ResponseAnalysisDto response = UserHelper.createResponseAnalysisDto(user);
//
//        Mockito.when(userService.getAnalysis(ArgumentMatchers.any(UserEntity.class)))
//                .thenReturn(response);
//
//        mockMvc.perform(
//                        MockMvcRequestBuilders.get(prefix + "/analysis")
//                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
//                                .accept(MediaType.APPLICATION_JSON)
//                )
//                .andExpect(MockMvcResultMatchers.status().isOk());
//
//        Mockito.verify(userService, Mockito.times(1)).getNotification(ArgumentMatchers.any(UserEntity.class));
//    }
//
//    @Test
//    @DisplayName("회원가입과 로그인 성공")
//    public void testLoginOrSignup() throws Exception {
//        RequestUserPostDTO request = new RequestUserPostDTO();
//        request.setSnsId("3526843826");
//        request.setSnsKind(SnsKind.GOOGLE.toString());
//        request.setName("김승용");
//        request.setToken("FCM_TOKEN");
//
//        JWT jwt = JWTHelper.createJWT();
//
//        Mockito.when(userService.signup(ArgumentMatchers.any(RequestUserPostDTO.class)))
//                .thenReturn(jwt);
//
//        mockMvc.perform(
//                        MockMvcRequestBuilders.post(prefix)
//                                .accept(MediaType.APPLICATION_JSON)
//                                .contentType(MediaType.APPLICATION_JSON)
//                                .content(objectMapper.writeValueAsString(request))
//                )
//                .andExpect(MockMvcResultMatchers.status().isCreated());
//
//        Mockito.verify(userService, Mockito.times(1)).signup(ArgumentMatchers.any(RequestUserPostDTO.class));
//    }
}

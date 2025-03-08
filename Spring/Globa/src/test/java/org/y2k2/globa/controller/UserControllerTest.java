package org.y2k2.globa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.dto.request.user.RequestUserPostDTO;
import org.y2k2.globa.dto.response.user.ResponseUserDto;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.filter.AuthenticationFilter;
import org.y2k2.globa.helper.JWTHelper;
import org.y2k2.globa.helper.UserHelper;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.type.SnsKind;
import org.y2k2.globa.util.jwt.JWT;


@Slf4j
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationFilter authenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String prefix = "/user";

    @Test
    @DisplayName("내 정보 가져오기")
    @WithAccount
    public void testGetMyInfo() throws Exception {
        JWT jwt = JWTHelper.createJWT();
        UserEntity user = UserHelper.createUser();
        ResponseUserDto response = UserHelper.createResponseUserDto(user);

        Mockito.when(userService.getUser(ArgumentMatchers.any(UserEntity.class)))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(prefix)
                                .header("Authorization", "Bearer " + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(userService, Mockito.times(1)).getUser(ArgumentMatchers.any(UserEntity.class));
    }

    @Test
    @DisplayName("회원가입과 로그인")
    public void testLoginOrSignup() throws Exception {
        RequestUserPostDTO request = new RequestUserPostDTO();
        request.setSnsId("3526843826");
        request.setSnsKind(SnsKind.GOOGLE.toString());
        request.setName("김승용");
        request.setToken("FCM_TOKEN");

        JWT jwt = JWTHelper.createJWT();

        Mockito.when(userService.signup(ArgumentMatchers.any(RequestUserPostDTO.class)))
                .thenReturn(jwt);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(prefix)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(userService, Mockito.times(1)).signup(ArgumentMatchers.any(RequestUserPostDTO.class));
    }
}

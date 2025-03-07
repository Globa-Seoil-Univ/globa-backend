package org.y2k2.globa.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration;
import org.springframework.boot.test.autoconfigure.restdocs.AutoConfigureRestDocs;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders;
import org.springframework.restdocs.operation.preprocess.Preprocessors;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.restdocs.payload.PayloadDocumentation;
import org.springframework.restdocs.snippet.Attributes;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.y2k2.globa.dto.request.user.RequestUserPostDTO;
import org.y2k2.globa.filter.AuthenticationFilter;
import org.y2k2.globa.service.UserService;
import org.y2k2.globa.type.SnsKind;
import org.y2k2.globa.util.CustomTimestamp;
import org.y2k2.globa.util.jwt.JWT;
import org.y2k2.globa.util.redis.RedisStore;

import java.time.LocalDateTime;


@WebMvcTest(
        controllers = UserController.class,
        excludeAutoConfiguration = {SecurityAutoConfiguration.class, SecurityFilterAutoConfiguration.class}
)
@AutoConfigureMockMvc(addFilters = false)
@AutoConfigureRestDocs
@ExtendWith(RestDocumentationExtension.class)
@Slf4j
public class UserControllerTest {
    @MockBean
    private UserService userService;

    @MockBean
    private AuthenticationFilter authenticationFilter;

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String prefix = "/user";

    private MockMvc mockMvc;

    @BeforeEach
    void setUp(WebApplicationContext webApplicationContext, RestDocumentationContextProvider restDocumentation) {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .apply(MockMvcRestDocumentation.documentationConfiguration(restDocumentation))
                .build();
    }

    @Test
    @DisplayName("회원가입과 로그인")
    public void testLoginOrSignup() throws Exception {
        RequestUserPostDTO request = new RequestUserPostDTO();
        request.setSnsId("3526843826");
        request.setSnsKind(SnsKind.GOOGLE.toString());
        request.setName("김승용");
        request.setToken("FCM_TOKEN");

        JWT jwt = JWT.builder()
                .grantType("Bearer")
                .accessToken("JWT_ACCESS_TOKEN")
                .accessTokenExpireTime(new CustomTimestamp().getTimestamp())
                .refreshToken("JWT_REFRESH_TOKEN")
                .refreshTokenExpireTime(new CustomTimestamp().getTimestamp())
                .build();

        Mockito.when(userService.signup(ArgumentMatchers.any(RequestUserPostDTO.class)))
                .thenReturn(jwt);

        mockMvc.perform(
                        RestDocumentationRequestBuilders.post(prefix)
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andDo(
                        MockMvcRestDocumentation.document(
                                "user/signupAndLogin",
                                Preprocessors.preprocessRequest(Preprocessors.prettyPrint()),
                                Preprocessors.preprocessResponse(Preprocessors.prettyPrint()),
                                PayloadDocumentation.requestFields(
                                        PayloadDocumentation
                                                .fieldWithPath("snsKind")
                                                .type(JsonFieldType.STRING)
                                                .description("SNS 종류 (KAKAO, GOOGLE)"),
                                        PayloadDocumentation
                                                .fieldWithPath("snsId")
                                                .type(JsonFieldType.STRING)
                                                .description("해당 SNS의 ID"),
                                        PayloadDocumentation
                                                .fieldWithPath("name")
                                                .type(JsonFieldType.STRING)
                                                .description("사용자 이름"),
                                        PayloadDocumentation
                                                .fieldWithPath("token")
                                                .type(JsonFieldType.STRING)
                                                .description("FCM 토큰"),
                                        PayloadDocumentation
                                                .fieldWithPath("profile")
                                                .type(JsonFieldType.STRING)
                                                .description("프로필 이미지 URL")
                                                .optional(),
                                        PayloadDocumentation
                                                .fieldWithPath("notification")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("알림 수신 여부")
                                                .attributes(
                                                        Attributes.key("default")
                                                                .value("false")
                                                )
                                                .optional(),
                                        PayloadDocumentation
                                                .fieldWithPath("eventNotification")
                                                .type(JsonFieldType.BOOLEAN)
                                                .description("이벤트 알림 수신 여부")
                                                .attributes(
                                                        Attributes.key("default")
                                                                .value("false")
                                                )
                                                .optional()
                                ),
                                PayloadDocumentation.responseFields(
                                        PayloadDocumentation
                                                .fieldWithPath("grantType")
                                                .type("Bearer")
                                                .description("토큰 타입 (Bearer)"),
                                        PayloadDocumentation
                                                .fieldWithPath("accessToken")
                                                .type(JsonFieldType.STRING)
                                                .description("액세스 토큰"),
                                        PayloadDocumentation
                                                .fieldWithPath("accessTokenExpireTime")
                                                .type(JsonFieldType.STRING)
                                                .description("액세스 토큰 만료 시간"),
                                        PayloadDocumentation
                                                .fieldWithPath("refreshToken")
                                                .type(JsonFieldType.STRING)
                                                .description("액세스 토큰 재발급을 위한 토큰"),
                                        PayloadDocumentation
                                                .fieldWithPath("refreshTokenExpireTime")
                                                .type(JsonFieldType.STRING)
                                                .description("리프레시 토큰 만료 시간")
                                )
                        )
                );

        Mockito.verify(userService, Mockito.times(1)).signup(ArgumentMatchers.any(RequestUserPostDTO.class));
    }
}

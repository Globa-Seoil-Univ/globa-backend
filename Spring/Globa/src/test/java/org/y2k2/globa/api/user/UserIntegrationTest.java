package org.y2k2.globa.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.survey.dto.request.RequestSurveyDto;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.application.user.dto.request.*;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.usecase.VerifyGoogleUseCase;
import org.y2k2.globa.application.user.usecase.VerifyKakaoUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.common.util.redis.RedisKey;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.AnalysisFixtureBuilder;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.user.data.AnalysisData;
import org.y2k2.globa.infrastructure.persistence.survey.type.SurveyType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;
import org.y2k2.globa.util.JWTTestProvider;
import org.y2k2.globa.util.RedisTestStore;

import java.util.Objects;

@Slf4j
public class UserIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private JWTTestProvider jwtTestProvider;
    @Autowired
    private RedisTestStore redisTestStore;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CacheManager cacheManager;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private AnalysisFixtureBuilder analysisFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private RoleFixture roleFixture;

    @MockBean
    private VerifyKakaoUseCase verifyKakaoUseCase;
    @MockBean
    private VerifyGoogleUseCase verifyGoogleUseCase;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = userFixture.create();
        setSecurityContext(user);
    }

    @Test
    @DisplayName("내 정보 조회 - 성공")
    @CacheEvict(value = "user", allEntries = true)
    public void getUser() throws Exception {
        folderFixture
                .withUser(user)
                .create();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUserDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseUserDto.class
        );

        Assertions.assertThat(response.userId()).isNotNull();
        Assertions.assertThat(response.name()).isEqualTo(user.getName());
        Assertions.assertThat(response.code()).isEqualTo(user.getCode());
        Assertions.assertThat(response.publicFolderId()).isNotNull();

        // Cache 확인
        UserEntity cachedUser = Objects.requireNonNull(cacheManager.getCache("user"))
                .get(response.userId(), UserEntity.class);

        Assertions.assertThat(cachedUser).isNotNull();
        Assertions.assertThat(cachedUser.getUserId()).isNotNull();
        Assertions.assertThat(cachedUser.getName()).isEqualTo(user.getName());
        Assertions.assertThat(cachedUser.getCode()).isEqualTo(user.getCode());
    }

    @Test
    @DisplayName("내 정보 조회 - 성공 (기본 폴더가 없는 경우)")
    void getUserWithoutDefaultFolder() throws Exception {
        folderRoleFixture.create();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseUserDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseUserDto.class
        );

        Assertions.assertThat(response.userId()).isNotNull();
        Assertions.assertThat(response.name()).isEqualTo(user.getName());
        Assertions.assertThat(response.code()).isEqualTo(user.getCode());
        Assertions.assertThat(response.publicFolderId()).isNotNull();
    }

    @Test
    @DisplayName("유저 검색 - 성공")
    void searchUser() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("code", user.getCode())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseUserSearchDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseUserSearchDto.class
        );

        Assertions.assertThat(response.userId()).isNotNull();
        Assertions.assertThat(response.code()).isEqualTo(user.getCode());
        Assertions.assertThat(response.name()).isEqualTo(user.getName());
    }

    @Test
    @DisplayName("유저 검색 - 없음")
    void searchUserNotFound() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("code", "NOTFOUND")
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        Assertions.assertThat(result.getResponse().getContentAsString()).isEmpty();
    }

    @Test
    @DisplayName("알림 정보 조회 - 성공")
    void getNotification() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/notification")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseNotificationSettingDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseNotificationSettingDto.class
        );

        Assertions.assertThat(response.eventNofi()).isNotNull();
        Assertions.assertThat(response.uploadNofi()).isNotNull();
        Assertions.assertThat(response.shareNofi()).isNotNull();
    }

    @Test
    @DisplayName("내 분석 정보 조회 - 성공 (퀴즈 기록 7일 이내)")
    void getAnalysis() throws Exception {
        AnalysisData data = analysisFixture
                .withUser(user)
                .withCreatedTime(new CustomTimestamp().getTimestamp())
                .build();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseAnalysisDto.class
        );

        Assertions.assertThat(response.keywords()).isNotNull();
        Assertions.assertThat(response.keywords().size()).isGreaterThan(0);
        Assertions.assertThat(response.keywords().get(0).word()).isEqualTo(data.keyword().getWord());
        Assertions.assertThat(response.keywords().get(0).importance()).isGreaterThan(0);

        Assertions.assertThat(response.quizGrades()).isNotNull();
        Assertions.assertThat(response.quizGrades().size()).isGreaterThan(0);
        Assertions.assertThat(response.quizGrades().get(0).quizGrade()).isGreaterThan(0);

        Assertions.assertThat(response.studyTimes()).isNotNull();
        Assertions.assertThat(response.studyTimes().size()).isGreaterThan(0);
        Assertions.assertThat(response.studyTimes().get(0).studyTime()).isGreaterThan(0);
    }
    
    @Test
    @DisplayName("내 분석 정보 조회 - 실패 (퀴즈 기록 7일 이후)")
    void getAnalysisAfter7Days() throws Exception {
        AnalysisData data = analysisFixture
                .withUser(user)
                .withCreatedTime(new CustomTimestamp().getTimestamp().minusDays(8))
                .build();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseAnalysisDto.class
        );

        Assertions.assertThat(response.keywords()).isNotNull();
        Assertions.assertThat(response.keywords().size()).isGreaterThan(0);
        Assertions.assertThat(response.keywords().get(0).word()).isEqualTo(data.keyword().getWord());
        Assertions.assertThat(response.keywords().get(0).importance()).isGreaterThan(0);

        Assertions.assertThat(response.studyTimes()).isNotNull();
        Assertions.assertThat(response.studyTimes().size()).isGreaterThan(0);
        Assertions.assertThat(response.studyTimes().get(0).studyTime()).isGreaterThan(0);

        Assertions.assertThat(response.quizGrades()).isEmpty();
    }

    @Test
    @DisplayName("내 분석 정보 조회 - 실패 (기록 없음)")
    void getAnalysisNoRecord() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
            result.getResponse().getContentAsString(),
            ResponseAnalysisDto.class
        );

        Assertions.assertThat(response.keywords()).isEmpty();
        Assertions.assertThat(response.studyTimes()).isEmpty();
        Assertions.assertThat(response.quizGrades()).isEmpty();
    }

    @Test
    @DisplayName("회원가입 - 성공 (카카오)")
    void signupKakao() throws Exception {
        folderRoleFixture.create();
        roleFixture
                .withName(UserRole.USER)
                .create();

        RequestUserPostDTO request = new RequestUserPostDTO(
                SnsKind.KAKAO.toString(),
                "SNS_ID",
                "NAME",
                "SNS_TOKEN",
                "PROFILE",
                true,
                true
        );

        Mockito.doNothing()
                .when(verifyGoogleUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.grantType").value("Bearer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessTokenExpireTime").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshTokenExpireTime").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 - 실패 (카카오)")
    void signupFailAuthKakao() throws Exception {
        folderRoleFixture.create();
        roleFixture
                .withName(UserRole.USER)
                .create();

        Mockito.doThrow(new CustomException(ErrorCode.INVALID_SNS_TOKEN))
                .when(verifyKakaoUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        RequestUserPostDTO request = new RequestUserPostDTO(
                SnsKind.KAKAO.toString(),
                "SNS_ID",
                "NAME",
                "SNS_TOKEN",
                "PROFILE",
                true,
                true
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVALID_SNS_TOKEN.getErrorCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("회원가입 - 성공 (구글)")
    void signupGoogle() throws Exception {
        folderRoleFixture.create();
        roleFixture
                .withName(UserRole.USER)
                .create();

        Mockito.doNothing()
                .when(verifyGoogleUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        RequestUserPostDTO request = new RequestUserPostDTO(
                SnsKind.GOOGLE.toString(),
                "SNS_ID",
                "NAME",
                "SNS_TOKEN",
                "PROFILE",
                true,
                true
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.grantType").value("Bearer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessTokenExpireTime").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshTokenExpireTime").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @DisplayName("회원가입 - 실패 (구글)")
    void signupFailAuthGoogle() throws Exception {
        folderRoleFixture.create();
        roleFixture
                .withName(UserRole.USER)
                .create();

        Mockito.doThrow(new CustomException(ErrorCode.INVALID_SNS_TOKEN))
                .when(verifyGoogleUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        RequestUserPostDTO request = new RequestUserPostDTO(
                SnsKind.GOOGLE.toString(),
                "SNS_ID",
                "NAME",
                "SNS_TOKEN",
                "PROFILE",
                true,
                true
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVALID_SNS_TOKEN.getErrorCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("회원가입 - 실패 (SNS 종류 없음)")
    void signupFailSnsKind() throws Exception {
        folderRoleFixture.create();
        roleFixture
                .withName(UserRole.USER)
                .create();

        RequestUserPostDTO request = new RequestUserPostDTO(
                "NOT_EXIST_SNS",
                "SNS_ID",
                "NAME",
                "SNS_TOKEN",
                "PROFILE",
                true,
                true
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("로그인 - 성공")
    void login() throws Exception {
        RequestUserPostDTO request = new RequestUserPostDTO(
                user.getSnsKind().toString(),
                user.getSnsId(),
                user.getName(),
                user.getNotificationToken(),
                user.getProfilePath(),
                true,
                true
        );

        Mockito.doNothing()
                .when(verifyKakaoUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        Mockito.doNothing()
                .when(verifyGoogleUseCase)
                .execute(ArgumentMatchers.any(VerifySnsCommand.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.grantType").value("Bearer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessTokenExpireTime").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshTokenExpireTime").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @DisplayName("Access Token 재발급 - 성공")
    void reissueAT() throws Exception {
        redisTestStore.deleteValue(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString()
        );

        JWT jwt = jwtTestProvider.generateToken(
                user.getUserId(),
                1,
                100
        );

        RequestRTRDto request = new RequestRTRDto(
                jwt.getRefreshToken()
        );

        redisTestStore.setValueExpire(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        Thread.sleep(1000);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/refresh")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshToken").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.grantType").value("Bearer"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.accessTokenExpireTime").exists())
                .andExpect(MockMvcResultMatchers.jsonPath("$.refreshTokenExpireTime").exists())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();
    }

    @Test
    @DisplayName("Access Token 재발급 - 실패 (Access Token 만료 안 됨)")
    void reissueATFail() throws Exception {
        redisTestStore.deleteValue(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString()
        );

        JWT jwt = jwtTestProvider.generateToken(
                user.getUserId(),
                100,
                100
        );

        RequestRTRDto request = new RequestRTRDto(
                jwt.getRefreshToken()
        );

        redisTestStore.setValueExpire(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString(),
                jwt.getRefreshToken(),
                jwt.getRefreshTokenExpireTime()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/refresh")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.ACTIVE_ACCESS_TOKEN.getErrorCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Access Token 재발급 - 실패 (Refresh Token 만료)")
    void reissueATFailRefresh() throws Exception {
        redisTestStore.deleteValue(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString()
        );

        JWT jwt = jwtTestProvider.generateToken(
                user.getUserId(),
                1,
                1
        );

        RequestRTRDto request = new RequestRTRDto(
                jwt.getRefreshToken()
        );

        redisTestStore.setValueExpire(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString(),
                jwt.getRefreshToken(),
                new CustomTimestamp().getTimestamp().plusDays(1)
        );

        Thread.sleep(1000);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/refresh")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.EXPIRED_REFRESH_TOKEN.getErrorCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("Access Token 재발급 - 실패 (Refresh Token 없음)")
    void reissueATFailRefreshNotFound() throws Exception {
        redisTestStore.deleteValue(
                RedisKey.REFRESH_KEY.getValue() + user.getUserId().toString()
        );

        JWT jwt = jwtTestProvider.generateToken(
                user.getUserId(),
                1,
                1
        );

        RequestRTRDto request = new RequestRTRDto(
                jwt.getRefreshToken()
        );

        Thread.sleep(1000);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/refresh")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.UNAUTHORIZED.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVALID_TOKEN.getErrorCode()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("FCM 토큰 추가 또는 수정 - 성공")
    void upsertFcmToken() throws Exception {
        RequestNotificationTokenDto request = new RequestNotificationTokenDto(
                "FCM_TOKEN"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.USER_PREFIX.getValue() + "/notification/token")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("알림 수정 - 성공")
    void modifyNotification() throws Exception {
        RequestNotificationSettingDto request = new RequestNotificationSettingDto(
                false,
                false,
                false
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.put(Constant.USER_PREFIX.getValue() + "/notification")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("이름 수정 - 성공")
    void modifyName() throws Exception {
        folderFixture
                .withUser(user)
                .create();

        RequestNameDto request = new RequestNameDto(
                "NEW_NAME"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(Constant.USER_PREFIX.getValue() + "/name")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("이름 수정 - 성공 (기본 폴더가 없는 경우)")
    void modifyNameWithoutDefaultFolder() throws Exception {
        folderRoleFixture.create();

        RequestNameDto request = new RequestNameDto(
                "NEW_NAME"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(Constant.USER_PREFIX.getValue() + "/name")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("프로필 수정 - 성공")
    void modifyProfile() throws Exception {
        RequestProfileImageDto request = new RequestProfileImageDto(
                new MockMultipartFile(
                        "profile",
                        "profile.jpg",
                        "image/jpeg",
                        "testdata".getBytes()
                )
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
    }

    @Test
    @DisplayName("프로필 수정 - 실패 (파일 형식 오류)")
    void modifyProfileFailFileType() throws Exception {
        RequestProfileImageDto request = new RequestProfileImageDto(
                new MockMultipartFile(
                        "profile",
                        "profile.txt",
                        "text/plain",
                        "testdata".getBytes()
                )
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
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("프로필 수정 - 실패 (파일 없음)")
    void modifyProfileFailFileNotFound() throws Exception {
        RequestProfileImageDto request = new RequestProfileImageDto(
                new MockMultipartFile(
                        "profile",
                        "",
                        "image/jpeg",
                        new byte[0]
                )
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
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    @DisplayName("회원 탈퇴 - 성공")
    void withdraw() throws Exception {
        RequestSurveyDto request = new RequestSurveyDto(
                SurveyType.BAC.name(),
                "정확성이 너무 떨어져요 + 기타 사유"
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.USER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("회원 탈퇴 - 실패 (설문조사 없음)")
    void withdrawFail() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.USER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value()))
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(HttpStatus.BAD_REQUEST.value()))
                .andDo(MockMvcResultHandlers.print());
    }
}

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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.user.command.ValidateSnsCommand;
import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.application.user.usecase.ValidateKakaoUseCase;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.AnalysisFixtureBuilder;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.user.data.AnalysisData;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

import java.util.Objects;

@Slf4j
public class UserIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
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
    private ValidateKakaoUseCase validateKakaoUseCase;

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
    void signup() throws Exception {
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
                .when(validateKakaoUseCase)
                .execute(ArgumentMatchers.any(ValidateSnsCommand.class));

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
}

package org.y2k2.globa.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.factory.*;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.user.AnalysisFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;
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
    private AnalysisFixture analysisFixture;
    @Autowired
    private FolderFixture folderFixture;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        user = userFixture.createFixture();
        setSecurityContext(user);
    }

    @Test
    @DisplayName("내 정보 조회 - 성공")
    @CacheEvict(value = "user", allEntries = true)
    public void getUser() throws Exception {
        folderFixture.createFixture(user);

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
        Assertions.assertThat(response.name()).isEqualTo(userFixture.getUserFactory().getName());
        Assertions.assertThat(response.code()).isEqualTo(userFixture.getUserFactory().getCode());
        Assertions.assertThat(response.publicFolderId()).isNotNull();

        // Cache 확인
        UserEntity cachedUser = Objects.requireNonNull(cacheManager.getCache("user"))
                .get(response.userId(), UserEntity.class);

        Assertions.assertThat(cachedUser).isNotNull();
        Assertions.assertThat(cachedUser.getUserId()).isNotNull();
        Assertions.assertThat(cachedUser.getName()).isEqualTo(userFixture.getUserFactory().getName());
        Assertions.assertThat(cachedUser.getCode()).isEqualTo(userFixture.getUserFactory().getCode());
    }

    @Test
    @DisplayName("유저 검색 - 성공")
    void searchUser() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("code", userFixture.getUserFactory().getCode())
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
        Assertions.assertThat(response.code()).isEqualTo(userFixture.getUserFactory().getCode());
        Assertions.assertThat(response.name()).isEqualTo(userFixture.getUserFactory().getName());
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
        analysisFixture.createFixture(user, new CustomTimestamp().getTimestamp());

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
        Assertions.assertThat(response.keywords().get(0).word()).isEqualTo(analysisFixture.getKeywordFactory().getWord());
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
        analysisFixture.createFixture(user, new CustomTimestamp().getTimestamp().minusDays(8));

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
        Assertions.assertThat(response.keywords().get(0).word()).isEqualTo(analysisFixture.getKeywordFactory().getWord());
        Assertions.assertThat(response.keywords().get(0).importance()).isGreaterThan(0);

        Assertions.assertThat(response.studyTimes()).isNotNull();
        Assertions.assertThat(response.studyTimes().size()).isGreaterThan(0);
        Assertions.assertThat(response.studyTimes().get(0).studyTime()).isGreaterThan(0);

        Assertions.assertThat(response.quizGrades()).isEmpty();
    }
}

package org.y2k2.globa.api.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.dto.response.ResponseUserSearchDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.factory.CreatorFactory;
import org.y2k2.globa.factory.FolderFactory;
import org.y2k2.globa.factory.UserFactory;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.user.repository.UserRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;

import java.util.Objects;

@Slf4j
@Import({ ControllerConfig.class, FolderRepositoryImpl.class })
@ActiveProfiles("test")
@AutoConfigureMockMvc(addFilters = false)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserIntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private CacheManager cacheManager;

    private UserFactory userFactory;
    private FolderFactory folderFactory;

    @BeforeEach
    void setUp() {
        userFactory = new UserFactory();
        UserEntity user = userFactory.createAndSave();

        folderFactory = FolderFactory.builder()
                .user(user)
                .build();
        folderFactory.createAndSave();
    }

    @Test
    @DisplayName("내 정보 조회 - 성공")
    @WithAccount
    @CacheEvict(value = "user", allEntries = true)
    public void getUser() throws Exception {
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
        Assertions.assertThat(response.name()).isEqualTo(userFactory.getName());
        Assertions.assertThat(response.code()).isEqualTo(userFactory.getCode());
        Assertions.assertThat(response.publicFolderId()).isNotNull();

        // Cache 확인
        UserEntity cachedUser = Objects.requireNonNull(cacheManager.getCache("user"))
                .get(response.userId(), UserEntity.class);

        Assertions.assertThat(cachedUser).isNotNull();
        Assertions.assertThat(cachedUser.getUserId()).isNotNull();
        Assertions.assertThat(cachedUser.getName()).isEqualTo(userFactory.getName());
        Assertions.assertThat(cachedUser.getCode()).isEqualTo(userFactory.getCode());
    }

    @Test
    @DisplayName("유저 검색 - 성공")
    @WithAccount
    void searchUser() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("code", userFactory.getCode())
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
        Assertions.assertThat(response.code()).isEqualTo(userFactory.getCode());
        Assertions.assertThat(response.name()).isEqualTo(userFactory.getName());
    }

    @Test
    @DisplayName("유저 검색 - 없음")
    @WithAccount
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
    @WithAccount
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
    @DisplayName("내 분석 정보 조회 - 성공")
    @WithAccount
    void getAnalysis() throws Exception {
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

//        Assertions.assertThat(response.keywords()).isNotNull();
//        Assertions.assertThat(response.quizGrades()).isNotNull();
//        Assertions.assertThat(response.studyTimes()).isNotNull();
//
//        Assertions.assertThat(response.keywords().size()).isGreaterThan(0);
//        Assertions.assertThat(response.quizGrades().size()).isGreaterThan(0);
//        Assertions.assertThat(response.studyTimes().size()).isGreaterThan(0);
    }
}

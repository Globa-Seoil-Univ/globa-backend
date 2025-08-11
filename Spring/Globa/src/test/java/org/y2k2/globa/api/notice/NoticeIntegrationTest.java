package org.y2k2.globa.api.notice;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.dummyimage.DummyImageFixture;
import org.y2k2.globa.fixture.notice.NoticeFixture;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Arrays;
import java.util.Objects;

@Slf4j
public class NoticeIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private UserRoleFixture userRoleFixture;
    @Autowired
    private RoleFixture roleFixture;
    @Autowired
    private NoticeFixture noticeFixture;
    @Autowired
    private DummyImageFixture dummyImageFixture;

    private RoleEntity admin;
    private RoleEntity editor;
    private RoleEntity viewer;
    private RoleEntity publicUser;
    private UserEntity myUser;

    @BeforeEach
    void setup() {
        myUser = userFixture.save(
                UserFixture.builder().build()
        );
        admin = roleFixture.getEntity(UserRole.ADMIN);
        editor = roleFixture.getEntity(UserRole.EDITOR);
        viewer = roleFixture.getEntity(UserRole.VIEWER);
        publicUser = roleFixture.getEntity(UserRole.USER);

        setSecurityContext(myUser);
    }

    @Test
    @DisplayName("공지사항 간단 조회 - 성공")
    @WithAccount
    void getIntroNotices_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(publicUser)
                        .build()
        );

        // 간단 공지사항 조회는 3개만 가져옴
        int noticeCount = 3;
        for (int i = 0; i < noticeCount; i++) {
            noticeFixture.save(
                    NoticeFixture.builder()
                            .title("공지사항 " + (i + 1))
                            .content("공지사항 내용 " + (i + 1))
                            .bgColor("#FFFFFF")
                            .user(myUser)
                            .thumbnail("/dummy/thumbnail/" + i + ".png")
                            .build()
            );
        }

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/notice/intro")
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNoticeIntroDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNoticeIntroDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.notices())
                .hasSize(noticeCount)
                .allSatisfy(notice -> {
                    Assertions.assertThat(notice.noticeId()).isNotNull();
                    Assertions.assertThat(notice.thumbnail()).isNotBlank();
                    Assertions.assertThat(notice.bgColor()).isNotBlank();
                });
    }

    @Test
    @DisplayName("공지사항 상세 조회 - 성공")
    @WithAccount
    void getNoticeDetail_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(publicUser)
                        .build()
        );

        String title = "공지사항 제목",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        // 공지사항 생성
        NoticeEntity notice = noticeFixture.save(
                NoticeFixture.builder()
                        .title(title)
                        .content(content)
                        .bgColor(bgColor)
                        .user(myUser)
                        .thumbnail("/dummy/thumbnail/notice.png")
                        .build()
        );

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/notice/{noticeId}", notice.getNoticeId())
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNoticeDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseNoticeDetailDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response)
                .as("공지사항 상세 조회가 성공해야 합니다.")
                .isNotNull();

        Assertions
                .assertThat(response.title())
                .as("공지사항 제목이 일치해야 합니다.")
                .isEqualTo(title);

        Assertions
                .assertThat(response.content())
                .as("공지사항 내용이 일치해야 합니다.")
                .isEqualTo(content);

        Assertions
                .assertThat(response.createdTime())
                .isNotNull();
    }

    @Test
    @DisplayName("공지사항 상세 조회 - 실패 (공지사항 X)")
    void getNoticeDetail_Fail_NotFoundNotice() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(publicUser)
                        .build()
        );

        Long nonExistentNoticeId = 999L; // 존재하지 않는 공지사항 ID

        mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/notice/{noticeId}", nonExistentNoticeId)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_NOTICE.getErrorCode()));
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (Admin)")
    @WithAccount
    void createNotice_Success_Admin() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(admin)
                        .build()
        );

        String title = "새 공지사항",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", title)
                                .param("content", content)
                                .param("bgColor", bgColor)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andReturn();

        Long createdNoticeId = Long.parseLong(
                Objects.requireNonNull(result.getResponse().getHeader("Location"))
                        .replace("/notice/", "")
        );

        log.info("Created notice ID = {}", createdNoticeId);

        // 생성된 공지사항 조회
        MvcResult retrievedResult = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/notice/{noticeId}", createdNoticeId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNoticeDetailDto response = objectMapper.readValue(
                retrievedResult.getResponse().getContentAsString(),
                ResponseNoticeDetailDto.class
        );

        log.info("Retrieved notice = {}", response);

        Assertions
                .assertThat(response)
                .as("공지사항 상세 조회가 성공해야 합니다.")
                .isNotNull();

        Assertions
                .assertThat(response.title())
                .as("공지사항 제목이 일치해야 합니다.")
                .isEqualTo(title);

        Assertions
                .assertThat(response.content())
                .as("공지사항 내용이 일치해야 합니다.")
                .isEqualTo(content);
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (Editor)")
    @WithAccount
    void createNotice_Success_Editor() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(editor)
                        .build()
        );

        String title = "새 공지사항",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", title)
                                .param("content", content)
                                .param("bgColor", bgColor)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andReturn();

        Long createdNoticeId = Long.parseLong(
                Objects.requireNonNull(result.getResponse().getHeader("Location"))
                        .replace("/notice/", "")
        );

        log.info("Created notice ID = {}", createdNoticeId);

        // 생성된 공지사항 조회
        MvcResult retrievedResult = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/notice/{noticeId}", createdNoticeId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNoticeDetailDto response = objectMapper.readValue(
                retrievedResult.getResponse().getContentAsString(),
                ResponseNoticeDetailDto.class
        );

        log.info("Retrieved notice = {}", response);

        Assertions
                .assertThat(response)
                .as("공지사항 상세 조회가 성공해야 합니다.")
                .isNotNull();

        Assertions
                .assertThat(response.title())
                .as("공지사항 제목이 일치해야 합니다.")
                .isEqualTo(title);

        Assertions
                .assertThat(response.content())
                .as("공지사항 내용이 일치해야 합니다.")
                .isEqualTo(content);
    }

    @Test
    @DisplayName("공지사항 생성 - 실패 (Viewer)")
    @WithAccount
    void createNotice_Fail_Viewer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(viewer)
                        .build()
        );

        String title = "새 공지사항",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", title)
                                .param("content", content)
                                .param("bgColor", bgColor)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("공지사항 생성 - 실패 (Public User)")
    @WithAccount
    void createNotice_Fail_PublicUser() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(publicUser)
                        .build()
        );

        String title = "새 공지사항",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", title)
                                .param("content", content)
                                .param("bgColor", bgColor)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("공지사항 생성 - 성공 (Dummy Image)")
    @WithAccount
    void createNotice_Success_WithDummyImage() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(admin)
                        .build()
        );

        String title = "새 공지사항",
                content = "공지사항 내용",
                bgColor = "#FFFFFF";

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        Long dummyImageId = dummyImageFixture.save(
                DummyImageFixture.builder()
                        .path("/dummy/image/dummy.png")
                        .build()
        ).getImageId();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", title)
                                .param("content", content)
                                .param("bgColor", bgColor)
                                .param("imageIds", String.valueOf(dummyImageId)) // 더미 이미지 ID 추가
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andReturn();

        Long createdNoticeId = Long.parseLong(
                Objects.requireNonNull(result.getResponse().getHeader("Location"))
                        .replace("/notice/", "")
        );

        log.info("Created notice ID = {}", createdNoticeId);

        // 생성된 공지사항 조회
        MvcResult retrievedResult = mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/notice/{noticeId}", createdNoticeId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseNoticeDetailDto response = objectMapper.readValue(
                retrievedResult.getResponse().getContentAsString(),
                ResponseNoticeDetailDto.class
        );

        log.info("Retrieved notice = {}", response);

        Assertions
                .assertThat(response)
                .as("공지사항 상세 조회가 성공해야 합니다.")
                .isNotNull();

        Assertions
                .assertThat(response.title())
                .as("공지사항 제목이 일치해야 합니다.")
                .isEqualTo(title);
    }

    @Test
    @DisplayName("공지사항 생성 - 실패 (잘못된 요청)")
    @WithAccount
    void createNotice_Fail_InvalidRequest() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(admin)
                        .build()
        );

        MockMultipartFile thumbnail = new MockMultipartFile(
                "thumbnail",
                "thumbnail.png",
                MediaType.IMAGE_PNG_VALUE,
                "dummy thumbnail content".getBytes()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .multipart("/notice")
                                .file(thumbnail)
                                .param("title", "") // 제목이 비어있는 경우
                                .param("content", "공지사항 내용")
                                .param("bgColor", "#FFFFFF")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.MULTIPART_FORM_DATA)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

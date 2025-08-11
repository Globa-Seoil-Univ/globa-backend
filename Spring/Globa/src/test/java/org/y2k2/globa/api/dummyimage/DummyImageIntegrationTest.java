package org.y2k2.globa.api.dummyimage;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class DummyImageIntegrationTest extends IntegrationTest {
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
    @DisplayName("더미 이미지 생성 - 성공 (Admin)")
    @WithAccount
    void createDummyImage_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(admin)
                        .build()
        );

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "dummy image content".getBytes()
        );

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/dummy/image")
                        .file(file)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseDummyImageDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDummyImageDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.imageId())
                .isNotNull();

        Assertions
                .assertThat(response.path())
                .isNotBlank();
    }

    @Test
    @DisplayName("더미 이미지 생성 - 성공 (Editor)")
    @WithAccount
    void createDummyImage_Success_Editor() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(editor)
                        .build()
        );

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "dummy image content".getBytes()
        );

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.multipart("/dummy/image")
                        .file(file)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseDummyImageDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDummyImageDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.imageId())
                .isNotNull();

        Assertions
                .assertThat(response.path())
                .isNotBlank();
    }

    @Test
    @DisplayName("더미 이미지 생성 - 실패 (Viewer)")
    @WithAccount
    void createDummyImage_Failure_Viewer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(viewer)
                        .build()
        );

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "dummy image content".getBytes()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/dummy/image")
                        .file(file)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("더미 이미지 생성 - 실패 (Public User)")
    @WithAccount
    void createDummyImage_Failure_PublicUser() throws Exception {
        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(myUser)
                        .role(publicUser)
                        .build()
        );

        MockMultipartFile file = new MockMultipartFile(
                "image",
                "test.png",
                "image/png",
                "dummy image content".getBytes()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.multipart("/dummy/image")
                        .file(file)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }
}

package org.y2k2.globa.api.folder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.folder.dto.request.RequestFolderNameDto;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Objects;

@Slf4j
public class FolderIntegrationTest extends IntegrationTest {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;

    private UserEntity user;
    private FolderRoleEntity owner;
    private FolderRoleEntity reader;

    @BeforeEach
    void setUp() {
        // Redis 초기화
        // Rollback을 해도 AUTO_INCREMENT는 초기화되지 않으므로, Redis를 사용하여 초기화
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        });

        user = userFixture.create();
        setSecurityContext(user);

        owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        reader = folderRoleFixture.getEntity(FolderRole.READER);
    }

    @AfterEach
    void tearDown() {
        cacheManager.getCacheNames().forEach(cacheName -> {
            Objects.requireNonNull(cacheManager.getCache(cacheName)).clear();
        });
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (폴더 없음)")
    void getFoldersEmpty() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("count", "2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseFolderDto.class
        );

        Assertions.assertThat(response)
                .as("폴더 조회에 성공하면 폴더 정보를 반환한다.")
                .isNotNull();
        Assertions.assertThat(response.getFolders())
                .as("폴더 조회 성공 및 기본 폴더가 없다면 생성 후 반환한다.")
                .isNotNull()
                .hasSize(1);
        Assertions.assertThat(response.getTotal())
                .as("폴더 조회에 성공하면 전체 개수를 반환한다.")
                .isNotNull()
                .isEqualTo(1);
        Assertions.assertThat(response.getFolders().get(0).getTitle())
                .as("기본 폴더가 포함되어야 한다.")
                .isEqualTo(user.getName());
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (기본 폴더 포함)")
    void getFolders() throws Exception {
        String title = "Default Folder";
        FolderEntity defaultFolder = folderFixture
                .withUser(user)
                .withTitle(title)
                .create();
        FolderEntity newFolder01 = folderFixture
                .withUser(user)
                .withTitle("New Folder 01")
                .create();
        FolderEntity newFolder02 = folderFixture
                .withUser(user)
                .withTitle("New Folder 02")
                .create();

        folderShareFixture
                .withFolder(defaultFolder)
                .withOwner(user)
                .withTarget(user)
                .withRole(owner)
                .create();
        folderShareFixture
                .withFolder(newFolder01)
                .withOwner(user)
                .withTarget(user)
                .withRole(reader)
                .create();
        folderShareFixture
                .withFolder(newFolder02)
                .withOwner(user)
                .withTarget(user)
                .withRole(reader)
                .create();

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("count", "2")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseFolderDto.class
        );

        Assertions.assertThat(response)
                .as("폴더 조회에 성공하면 폴더 정보를 반환한다.")
                .isNotNull();
        Assertions.assertThat(response.getFolders())
                .as("폴더 조회에 성공하면 폴더 정보를 반환한다.")
                .isNotNull()
                .hasSize(2);
        Assertions.assertThat(response.getTotal())
                .as("폴더 조회에 성공하면 전체 개수를 반환한다.")
                .isNotNull()
                .isEqualTo(3);
        Assertions.assertThat(response.getFolders().get(0).getFolderId())
                .as("기본 폴더가 포함되어야 한다.")
                .isEqualTo(defaultFolder.getFolderId());
        Assertions.assertThat(response.getFolders().get(1).getFolderId())
                .as("첫 번째 폴더가 포함되어야 한다.")
                .isEqualTo(newFolder01.getFolderId());
        Assertions.assertThat(response.getFolders().get(1).getFolderId())
                .as("두 번째 폴더는 개수로 인해 제외되어야 한다.")
                .isNotEqualTo(newFolder02.getFolderId());
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (기본 폴더 미포함)")
    void getFoldersWithoutDefault() throws Exception {
        String title = "Default Folder";
        FolderEntity defaultFolder = folderFixture
                .withUser(user)
                .withTitle(title)
                .create();
        FolderEntity newFolder01 = folderFixture
                .withUser(user)
                .withTitle("New Folder 01")
                .create();
        FolderEntity newFolder02 = folderFixture
                .withUser(user)
                .withTitle("New Folder 02")
                .create();

        folderShareFixture
                .withFolder(defaultFolder)
                .withOwner(user)
                .withTarget(user)
                .withRole(owner)
                .create();
        folderShareFixture
                .withFolder(newFolder01)
                .withOwner(user)
                .withTarget(user)
                .withRole(reader)
                .create();
        folderShareFixture
                .withFolder(newFolder02)
                .withOwner(user)
                .withTarget(user)
                .withRole(reader)
                .create();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.FOLDER_PREFIX.getValue())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "2")
                                .param("count", "1")
                        )
                        .andDo(MockMvcResultHandlers.print())
                        .andExpect(MockMvcResultMatchers.status().isOk())
                        .andReturn();

        ResponseFolderDto response = objectMapper.readValue(
                        result.getResponse().getContentAsString(),
                        ResponseFolderDto.class
        );

        Assertions.assertThat(response)
                .as("폴더 조회에 성공하면 폴더 정보를 반환한다.")
                .isNotNull();
        Assertions.assertThat(response.getFolders())
                .as("1개만 조회했으므로, 폴더 정보는 1개만 반환한다.")
                .isNotNull()
                .hasSize(1);
        Assertions.assertThat(response.getTotal())
                .as("폴더 조회에 성공하면 전체 개수를 반환한다.")
                .isNotNull()
                .isEqualTo(3);
        Assertions.assertThat(response.getFolders().get(0).getFolderId())
                .as("페이지가 2 이므로, 두 번째 폴더가 포함되어야 한다.")
                .isEqualTo(newFolder02.getFolderId());
        Assertions.assertThat(response.getFolders().get(0).getFolderId())
                .as("페이지가 2 이므로, 첫 번째 폴더는 개수로 인해 제외되어야 한다.")
                .isNotEqualTo(newFolder01.getFolderId());
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 X)")
    void createFolder() throws Exception {
        String title = "New Folder";
        RequestFolderPostDto request = new RequestFolderPostDto(title, null);

        mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.header().string("Location", "/folder"));
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 O)")
    void createFolderWithShare() throws Exception {
        String title = "New Folder";
        String code = userFixture
                .create()
                .getCode();
        RequestFolderPostDto request = new RequestFolderPostDto(
                title,
                List.of(new RequestFolderPostDto.ShareTarget(FolderRole.EDITOR.toString(), code))
        );

        mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andExpect(MockMvcResultMatchers.header().string("Location", "/folder"));
    }

    @Test
    @DisplayName("폴더 이름 수정 - 성공")
    void updateFolderName() throws Exception {
        FolderEntity folder = folderFixture
                .withUser(user)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withRole(owner)
                .withOwner(user)
                .withTarget(user)
                .create();
        RequestFolderNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFolderNameDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_PREFIX.getValue() + "/{folderId}/name", folder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("폴더 이름 수정 - 실패 (권한 X)")
    void updateFolderNameWithoutAuth() throws Exception {
        FolderEntity folder = folderFixture
                .withUser(user)
                .create();
        RequestFolderNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFolderNameDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_PREFIX.getValue() + "/{folderId}/name", folder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("폴더 삭제 - 성공")
    void deleteFolder() throws Exception {
        FolderEntity _defaultFolder = folderFixture
                .withUser(user)
                .create();
        FolderEntity folder = folderFixture
                .withUser(user)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withRole(owner)
                .withOwner(user)
                .withTarget(user)
                .create();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_PREFIX.getValue() + "/{folderId}", folder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("폴더 삭제 - 실패 (기본 폴더)")
    void deleteDefaultFolder() throws Exception {
        FolderEntity folder = folderFixture
                .withUser(user)
                .create();
        folderShareFixture
                .withFolder(folder)
                .withRole(owner)
                .withOwner(user)
                .withTarget(user)
                .create();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_PREFIX.getValue() + "/{folderId}", folder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_FOLDER.getErrorCode()));
    }

    @Test
    @DisplayName("폴더 삭제 - 실패 (권한 없음)")
    void deleteFolderWithoutAuth() throws Exception {
        FolderEntity folder = folderFixture
                .withUser(user)
                .create();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_PREFIX.getValue() + "/{folderId}", folder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }
}

package org.y2k2.globa.api.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
public class RecordIntegrationTest extends IntegrationTest {
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
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;
    @Autowired
    private RecordFixture recordFixture;

    private UserEntity user;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private FolderEntity myFolder;
    private RecordEntity myRecord;

    @BeforeEach
    public void setUp() {
        Optional.ofNullable(cacheManager.getCache("folderRole")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("analysis")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("sections")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("summaries")).ifPresent(Cache::clear);

        user = userFixture.create();
        owner = folderRoleFixture
                .withRole(FolderRole.OWNER)
                .create();
        editor = folderRoleFixture
                .withRole(FolderRole.EDITOR)
                .create();
        reader = folderRoleFixture
                .withRole(FolderRole.READER)
                .create();
        myFolder = folderFixture
                .withUser(user)
                .create();
        myRecord = recordFixture
                .withUser(user)
                .withFolder(myFolder)
                .create();

        folderShareFixture.
                withOwner(user)
                .withTarget(user)
                .withFolder(myFolder)
                .withRole(owner)
                .create();
    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(cacheManager.getCache("folderRole")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("analysis")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("sections")).ifPresent(Cache::clear);
        Optional.ofNullable(cacheManager.getCache("summaries")).ifPresent(Cache::clear);
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 성공 (내 폴더 O)")
    @WithAccount
    void getRecordsInFolder() throws Exception {
        UserEntity otherUser = userFixture.create();

        RecordEntity otherRecord = recordFixture
                .withUser(otherUser)
                .withFolder(myFolder)
                .create();

        RecordEntity myOtherRecord = recordFixture
                .withUser(user)
                .withFolder(myFolder)
                .create();

        folderShareFixture
                .withOwner(user)
                .withTarget(otherUser)
                .withFolder(myFolder)
                .withRole(editor)
                .create();

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), myFolder.getFolderId())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsByFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsByFolderDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 3개의 문서가 조회되어야 합니다.")
                .isEqualTo(3);

        Assertions.assertThat(response.isOwner())
                .as("내가 소유한 폴더의 문서 조회이므로 true여야 합니다.")
                .isTrue();

        Assertions.assertThat(response.records())
                .as("내 폴더에 속한 모든 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                    Assertions.assertThat(record.recordId())
                            .isIn(myRecord.getRecordId(), myOtherRecord.getRecordId(), otherRecord.getRecordId())
                );
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 성공 (내 폴더 X)")
    @WithAccount
    void getRecordsInFolderNotMyFolder() throws Exception {
        UserEntity otherUser = userFixture.create();

        FolderEntity otherFolder = folderFixture
                .withUser(otherUser)
                .create();

        RecordEntity myRecordInOtherFolder = recordFixture
                .withUser(user)
                .withFolder(otherFolder)
                .create();

        RecordEntity otherRecord = recordFixture
                .withUser(otherUser)
                .withFolder(otherFolder)
                .create();

        folderShareFixture
                .withOwner(otherUser)
                .withTarget(otherUser)
                .withFolder(otherFolder)
                .withRole(owner)
                .create();

        folderShareFixture
                .withOwner(otherUser)
                .withTarget(user)
                .withFolder(otherFolder)
                .withRole(editor)
                .create();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), otherFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsByFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsByFolderDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 2개의 문서가 조회되어야 합니다.")
                .isEqualTo(2);

        Assertions.assertThat(response.isOwner())
                .as("내가 소유한 폴더가 아니므로, false여야 합니다.")
                .isFalse();

        Assertions.assertThat(response.records())
                .as("내 폴더에 속한 모든 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                        Assertions.assertThat(record.recordId())
                                .isIn(myRecordInOtherFolder.getRecordId(), otherRecord.getRecordId())
                );
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 실패 (내 폴더 X, 권한 X)")
    @WithAccount
    void getRecordsInFolderNotMyFolderWithoutPermission() throws Exception {
        UserEntity otherUser = userFixture.create();

        FolderEntity otherFolder = folderFixture
                .withUser(otherUser)
                .create();

        recordFixture
                .withUser(user)
                .withFolder(otherFolder)
                .create();

        recordFixture
                .withUser(otherUser)
                .withFolder(otherFolder)
                .create();

        folderShareFixture
                .withOwner(otherUser)
                .withTarget(otherUser)
                .withFolder(otherFolder)
                .withRole(owner)
                .create();

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), otherFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("최근 문서 조회 - 성공")
    @WithAccount
    void getRecentRecords() throws Exception {
        UserEntity otherUser = userFixture.create();
        FolderEntity otherFolder = folderFixture
                .withUser(otherUser)
                .create();
        RecordEntity otherRecord = recordFixture
                .withUser(otherUser)
                .withFolder(otherFolder)
                .withTitle("otherRecord")
                .create();

        folderShareFixture
                .withOwner(otherUser)
                .withTarget(otherUser)
                .withFolder(otherFolder)
                .withRole(owner)
                .create();

        folderShareFixture
                .withOwner(otherUser)
                .withTarget(user)
                .withFolder(otherFolder)
                .withRole(reader)
                .create();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/recent")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 2개의 문서가 조회되어야 합니다.")
                .isEqualTo(2);

        Assertions.assertThat(response.records())
                .as("내가 소유한 폴더의 문서와 공유된 폴더의 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                        Assertions.assertThat(record.recordId())
                                .isIn(myRecord.getRecordId(), otherRecord.getRecordId())
                );
    }
}

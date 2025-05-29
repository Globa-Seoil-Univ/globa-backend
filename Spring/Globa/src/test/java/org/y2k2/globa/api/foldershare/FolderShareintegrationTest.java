package org.y2k2.globa.api.foldershare;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class FolderShareintegrationTest extends IntegrationTest {
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

    private UserEntity user;
    private UserEntity otherUser;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private FolderEntity myFolder;
    private FolderEntity otherFolder;

    @BeforeEach
    public void setUp() {
        user = userFixture.create();
        otherUser = userFixture.create();
        owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        editor = folderRoleFixture.getEntity(FolderRole.EDITOR);
        reader = folderRoleFixture.getEntity(FolderRole.READER);

        myFolder = folderFixture
                .withUser(user)
                .create();
        otherFolder = folderFixture
                .withUser(otherUser)
                .create();

        folderShareFixture.
                withOwner(user)
                .withTarget(user)
                .withFolder(myFolder)
                .withRole(owner)
                .create();
        folderShareFixture
                .withOwner(otherUser)
                .withTarget(otherUser)
                .withFolder(otherFolder)
                .withRole(owner)
                .create();

        setSecurityContext(user);
    }

    @Test
    @DisplayName("공유된 사용자 조회 - 성공")
    @WithAccount
    void getSharedUsers() throws Exception {
        Long folderId = myFolder.getFolderId();
        int page = 1,
            count = 10;

        folderShareFixture
                .withOwner(user)
                .withTarget(otherUser)
                .withFolder(myFolder)
                .withRole(editor)
                .withInvitationStatus(InvitationStatus.PENDING)
                .create();

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user", folderId)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseFolderShareUserDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseFolderShareUserDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.total())
                .as("소유자 및 편집자 총 2개가 조회되어야 합니다.")
                .isEqualTo(2L);

        Assertions
                .assertThat(response.users())
                .as("소유자와 편집자가 조회되어야 합니다.")
                .hasSize(2)
                .allSatisfy(responseUser -> {
                    Assertions
                            .assertThat(responseUser.roleId())
                            .as("소유자와 편집자 중 하나는 소유자여야 합니다.")
                            .isIn(owner.getRoleId(), editor.getRoleId());

                    Assertions
                            .assertThat(responseUser.user().userId())
                            .as("소유자와 편집자 중 하나는 현재 사용자여야 합니다.")
                            .isIn(user.getUserId(), otherUser.getUserId());

                    Assertions
                            .assertThat(responseUser.invitationStatus())
                            .as("소유자는 ACCEPT, 편집자는 초대 상태가 PENDING이어야 합니다.")
                            .isIn(InvitationStatus.ACCEPT, InvitationStatus.PENDING);
                });
    }
}

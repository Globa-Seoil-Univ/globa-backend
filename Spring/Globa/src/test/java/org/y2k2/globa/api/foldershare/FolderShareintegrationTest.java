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
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
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
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
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
        user = userFixture.save(
                UserFixture
                        .builder()
                        .build()
        );
        otherUser = userFixture.save(
                UserFixture
                        .builder()
                        .name("Other User")
                        .build()
        );
        owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        editor = folderRoleFixture.getEntity(FolderRole.EDITOR);
        reader = folderRoleFixture.getEntity(FolderRole.READER);

        myFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .build()
        );
        otherFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(otherUser)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(user)
                        .folder(myFolder)
                        .role(owner)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        setSecurityContext(user);
    }

    @Test
    @DisplayName("공유된 사용자 조회 - 성공")
    @WithAccount
    void getSharedUsers() throws Exception {
        Long folderId = myFolder.getFolderId();
        int page = 1,
                count = 10;

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(editor)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

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

    @Test
    @DisplayName("공유된 사용자 조회 - 실패 (존재하지 않는 폴더)")
    @WithAccount
    void getSharedUsersWithNonExistentFolder() throws Exception {
        Long folderId = 999L;
        int page = 1,
                count = 10;

        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user", folderId)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("공유된 사용자 조회 - 실패 (소유자 X)")
    @WithAccount
    void getSharedUsersFailedOwner() throws Exception {
        Long folderId = otherFolder.getFolderId();
        int page = 1,
            count = 10;

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.get(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user", folderId)
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 공유 초대 - 성공")
    @WithAccount
    void invite() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.READER.name());

        log.info("Inviting user with ID = {}, Role = {}", targetUserId, request.role());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string(
                        "Location", "/folder/" + folderId + "/share/user"
                ));
    }

    @Test
    @DisplayName("사용자 공유 초대 - 실패 (존재하지 않는 폴더)")
    @WithAccount
    void inviteNonExistentFolder() throws Exception {
        Long folderId = 999L; // 존재하지 않는 폴더 ID
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.READER.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_FOLDER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 공유 초대 - 실패 (소유자 X)")
    @WithAccount
    void inviteFailedOwner() throws Exception {
        Long folderId = otherFolder.getFolderId(); // 다른 사용자의 폴더
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.READER.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 공유 초대 - 실패 (이미 초대된 사용자)")
    @WithAccount
    void inviteAlreadyInvitedUser() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.READER.name());

        // 이미 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.SHARE_USER_DUPLICATED.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 공유 초대 - 실패 (잘못된 역할)")
    @WithAccount
    void inviteInvalidRole() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto("INVALID_ROLE"); // 잘못된 역할

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 권한 변경 - 성공")
    @WithAccount
    void editShareUserRole() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.EDITOR.name());

        // 이미 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("사용자 권한 변경 - 실패 (자신 권한 변경 시도)")
    @WithAccount
    void editShareUserRoleSelf() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = user.getUserId(); // 자신의 ID
        RequestInviteDto request = new RequestInviteDto(FolderRole.EDITOR.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVITE_BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 권한 변경 - 실패 (소유자 X)")
    @WithAccount
    void editShareUserRoleFailedOwner() throws Exception {
        Long folderId = otherFolder.getFolderId(); // 다른 사용자의 폴더
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.EDITOR.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 권한 변경 - 실패 (존재하지 않은 초대)")
    @WithAccount
    void editShareUserRoleNonExistentInvitation() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId(); // 존재하지 않는 초대
        RequestInviteDto request = new RequestInviteDto(FolderRole.EDITOR.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_SHARE.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 권한 변경 - 실패 (잘못된 역할)")
    @WithAccount
    void editShareUserRoleInvalidRole() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto("INVALID_ROLE"); // 잘못된 역할

        // 이미 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("사용자 권한 변경 - 실패 (존재하지 않는 폴더)")
    @WithAccount
    void editShareUserRoleNonExistentFolder() throws Exception {
        Long folderId = 999L; // 존재하지 않는 폴더 ID
        Long targetUserId = otherUser.getUserId();
        RequestInviteDto request = new RequestInviteDto(FolderRole.EDITOR.name());

        mockMvc
                .perform(
                        MockMvcRequestBuilders.patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 초대 삭제 - 성공")
    @WithAccount
    void deleteShareUser() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId();

        // 이미 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("사용자 초대 삭제 - 실패 (자신 초대 삭제)")
    @WithAccount
    void deleteShareUserSelf() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = user.getUserId(); // 자신의 ID

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVITE_BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 초대 삭제 - 실패 (소유자 X)")
    @WithAccount
    void deleteShareUserFailedOwner() throws Exception {
        Long folderId = otherFolder.getFolderId(); // 다른 사용자의 폴더
        Long targetUserId = otherUser.getUserId();

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 초대 삭제 - 실패 (존재하지 않는 초대)")
    @WithAccount
    void deleteShareUserNonExistentInvitation() throws Exception {
        Long folderId = myFolder.getFolderId();
        Long targetUserId = otherUser.getUserId(); // 존재하지 않는 초대

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_SHARE.getErrorCode()));
    }

    @Test
    @DisplayName("사용자 초대 삭제 - 실패 (존재하지 않는 폴더)")
    @WithAccount
    void deleteShareUserNonExistentFolder() throws Exception {
        Long folderId = 999L; // 존재하지 않는 폴더 ID
        Long targetUserId = otherUser.getUserId();

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetUserId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_FOLDER_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("초대 수락 - 성공")
    @WithAccount
    void acceptInvitation() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 초대된 사용자로 설정
        FolderShareEntity invitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, invitation.getShareId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("초대 수락 - 실패 (존재하지 않는 초대)")
    @WithAccount
    void acceptInvitationNonExistentShare() throws Exception {
        Long folderId = otherFolder.getFolderId();
        Long shareId = 999L; // 존재하지 않는 초대 ID

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, shareId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_SHARE.getErrorCode()));
    }

    @Test
    @DisplayName("초대 수락 - 실패 (초대 ID 불일치)")
    @WithAccount
    void acceptInvitationShareIdMismatch() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        // 다른 사용자의 초대
        UserEntity otherUser2 = userFixture.save(
                UserFixture
                        .builder()
                        .name("Another User")
                        .build()
        );
        FolderShareEntity fakeInvitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser2)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, fakeInvitation.getShareId()) // 잘못된 shareId
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_SHARE_ID.getErrorCode()));
    }

    @Test
    @DisplayName("초대 수락 - 실패 (이미 수락된 초대)")
    @WithAccount
    void acceptInvitationAlreadyAccepted() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 이미 수락된 초대 설정
        FolderShareEntity invitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, invitation.getShareId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVITE_ACCEPT_BAD_REQUEST.getErrorCode()));
    }

    @Test
    @DisplayName("초대 거절 - 성공")
    @WithAccount
    void refuseInvitation() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 초대된 사용자로 설정
        FolderShareEntity invitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, invitation.getShareId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("초대 거절 - 실패 (존재하지 않는 초대)")
    @WithAccount
    void refuseInvitationNonExistentShare() throws Exception {
        Long folderId = otherFolder.getFolderId();
        Long shareId = 999L; // 존재하지 않는 초대 ID

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, shareId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_SHARE.getErrorCode()));
    }

    @Test
    @DisplayName("초대 거절 - 실패 (초대 ID 불일치)")
    @WithAccount
    void refuseInvitationShareIdMismatch() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 초대된 사용자로 설정
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        // 다른 사용자의 초대
        UserEntity otherUser2 = userFixture.save(
                UserFixture
                        .builder()
                        .name("Another User")
                        .build()
        );
        FolderShareEntity fakeInvitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser2)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.PENDING)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, fakeInvitation.getShareId()) // 잘못된 shareId
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_SHARE_ID.getErrorCode()));
    }

    @Test
    @DisplayName("초대 거절 - 실패 (이미 수락된 초대)")
    @WithAccount
    void refuseInvitationAlreadyAccepted() throws Exception {
        Long folderId = otherFolder.getFolderId();

        // 이미 수락된 초대 설정
        FolderShareEntity invitation = folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        mockMvc
                .perform(
                        MockMvcRequestBuilders.delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, invitation.getShareId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INVITE_ACCEPT_BAD_REQUEST.getErrorCode()));
    }
}

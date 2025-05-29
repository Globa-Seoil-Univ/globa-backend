package org.y2k2.globa.api.foldershare;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.api.FolderShareController;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.foldershare.dto.request.RequestInviteDto;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.application.foldershare.service.*;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = FolderShareController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FolderShareControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetFolderSharesService getFolderSharesService;
    @MockBean
    private InviteFolderShareService inviteFolderShareService;
    @MockBean
    private UpdateFolderShareService updateFolderShareService;
    @MockBean
    private DeleteFolderShareService deleteFolderShareService;
    @MockBean
    private AcceptInvitationService acceptInvitationService;
    @MockBean
    private RefuseInvitationService refuseInvitationService;

    @Test
    @DisplayName("공유된 사용자 조회 - 성공")
    @WithAccount
    void getFolderSharesSuccess() throws Exception {
        Long folderId = 1L,
                userId = 1L;
        int page = 1,
                count = 10;
        ResponseFolderShareUserDto response = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseFolderShareUserDto.class);

        Mockito
                .when(getFolderSharesService.get(folderId, page, count, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user", folderId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getFolderSharesService, Mockito.times(1))
                .get(folderId, page, count, userId);
    }

    @Test
    @DisplayName("사용자 초대 - 성공")
    @WithAccount
    void inviteShare() throws Exception {
        Long folderId = 1L,
                ownerId = 1L,
                targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.EDITOR.name());

        Mockito
                .doNothing()
                .when(inviteFolderShareService)
                .invite(folderId, targetId, dto, ownerId);

        log.info("Invite request send for folderId = {}, targetId = {}", folderId, targetId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(inviteFolderShareService, Mockito.times(1))
                .invite(folderId, targetId, dto, ownerId);
    }

    @Test
    @DisplayName("사용자 초대 - 실패 (잘못된 권한 요청)")
    void inviteShareFail() throws Exception {
        Long folderId = 1L,
                ownerId = 1L,
                targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto("INVALID_ROLE");

        log.info("Invite request send for folderId = {}, targetId = {}", folderId, targetId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(inviteFolderShareService, Mockito.times(0))
                .invite(folderId, targetId, dto, ownerId);
    }

    @Test
    @DisplayName("공유된 사용자 권한 수정 - 성공")
    @WithAccount
    void updateShare() throws Exception {
        Long folderId = 1L,
                ownerId = 1L,
                targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto(FolderRole.READER.name());

        Mockito
                .doNothing()
                .when(updateFolderShareService)
                .update(folderId, targetId, dto, ownerId);

        log.info("Update request send for folderId = {}, targetId = {}", folderId, targetId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateFolderShareService, Mockito.times(1))
                .update(folderId, targetId, dto, ownerId);
    }

    @Test
    @DisplayName("공유된 사용자 권한 수정 - 실패 (잘못된 권한 요청)")
    @WithAccount
    void updateShareFail() throws Exception {
        Long folderId = 1L,
                ownerId = 1L,
                targetId = 2L;

        RequestInviteDto dto = new RequestInviteDto("INVALID_ROLE");

        log.info("Update request send for folderId = {}, targetId = {}", folderId, targetId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .patch(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(updateFolderShareService, Mockito.times(0))
                .update(folderId, targetId, dto, ownerId);
    }

    @Test
    @DisplayName("공유된 사용자 삭제 - 성공")
    @WithAccount
    void deleteShare() throws Exception {
        Long folderId = 1L,
                ownerId = 1L,
                targetId = 2L;

        Mockito
                .doNothing()
                .when(deleteFolderShareService)
                .delete(folderId, targetId, ownerId);

        log.info("Delete request send for folderId = {}, targetId = {}", folderId, targetId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/user/{userId}", folderId, targetId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(deleteFolderShareService, Mockito.times(1))
                .delete(folderId, targetId, ownerId);
    }

    @Test
    @DisplayName("초대 수락 - 성공")
    @WithAccount
    void acceptInvitation() throws Exception {
        Long folderId = 1L,
                shareId = 2L;

        Mockito
                .doNothing()
                .when(acceptInvitationService)
                .accept(Mockito.eq(folderId), Mockito.eq(shareId), Mockito.any(CustomUserDetails.class));

        log.info("Accept invitation request send for folderId = {}", folderId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, shareId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(acceptInvitationService, Mockito.times(1))
                .accept(Mockito.eq(folderId), Mockito.eq(shareId), Mockito.any(CustomUserDetails.class));
    }

    @Test
    @DisplayName("초대 거절 - 성공")
    @WithAccount
    void refuseInvitation() throws Exception {
        Long folderId = 1L,
                shareId = 2L,
                userId = 1L;

        Mockito
                .doNothing()
                .when(refuseInvitationService)
                .refuse(folderId, shareId, userId);

        log.info("Refuse invitation request send for folderId = {}", folderId);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .delete(Constant.FOLDER_SHARE_PREFIX.getValue() + "/{shareId}", folderId, shareId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(refuseInvitationService, Mockito.times(1))
                .refuse(folderId, shareId, userId);
    }
}

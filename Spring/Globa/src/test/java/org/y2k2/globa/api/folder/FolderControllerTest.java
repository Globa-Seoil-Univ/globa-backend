package org.y2k2.globa.api.folder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
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
import org.y2k2.globa.api.FolderController;
import org.y2k2.globa.application.folder.dto.request.RequestFolderNameDto;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.application.folder.service.CreateFolderService;
import org.y2k2.globa.application.folder.service.DeleteFolderService;
import org.y2k2.globa.application.folder.service.GetFoldersService;
import org.y2k2.globa.application.folder.service.UpdateFolderNameService;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

import java.util.List;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = FolderController.class)
@AutoConfigureMockMvc(addFilters = false)
public class FolderControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetFoldersService getFoldersService;
    @MockBean
    private CreateFolderService createFolderService;
    @MockBean
    private UpdateFolderNameService updateFolderNameService;
    @MockBean
    private DeleteFolderService deleteFolderService;

    @Test
    @DisplayName("내 폴더 조회 - 성공")
    @WithAccount
    void getFolders() throws Exception {
        ResponseFolderDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(ResponseFolderDto.class);

        Mockito.when(getFoldersService.getFolders(
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyInt(),
                        ArgumentMatchers.anyLong()
                ))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .param("page", "1")
                        .param("count", "10")
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk());

        Mockito.verify(getFoldersService, Mockito.times(1))
                .getFolders(ArgumentMatchers.anyInt(), ArgumentMatchers.anyInt(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 X)")
    @WithAccount
    void createFolder() throws Exception {
        String folderName = "testFolder";
        RequestFolderPostDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.class)
                .set("title", folderName)
                .set("shareTargets", null)
                .sample();
        FolderShareEntity folderShare = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FolderShareEntity.class);

        Mockito.when(createFolderService.create(
                        ArgumentMatchers.anyString(),
                        ArgumentMatchers.anyLong()
                ))
                .thenReturn(folderShare);

        mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(createFolderService, Mockito.times(1))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
        Mockito.verify(createFolderService, Mockito.times(0))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 O)")
    @WithAccount
    void createFolderWithShare() throws Exception {
        String folderName = "testFolder";
        RequestFolderPostDto.ShareTarget shareTarget = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.ShareTarget.class)
                .set("role", FolderRole.READER.toString())
                .set("code", "ABCABC")
                .sample();
        RequestFolderPostDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.class)
                .set("title", folderName)
                .set("shareTargets", List.of(shareTarget))
                .sample();

        Mockito.doNothing()
                .when(createFolderService)
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyLong());

        mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isCreated());

        Mockito.verify(createFolderService, Mockito.times(0))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
        Mockito.verify(createFolderService, Mockito.times(1))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("폴더 생성 - 실패 (권한 X)")
    @WithAccount
    void createFolderWithInvalidRole() throws Exception {
        String folderName = "testFolder";
        RequestFolderPostDto.ShareTarget shareTarget = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.ShareTarget.class)
                .set("role", "INVALID_ROLE")
                .sample();
        RequestFolderPostDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.class)
                .set("title", folderName)
                .set("shareTargets", List.of(shareTarget))
                .sample();

        mockMvc.perform(
                MockMvcRequestBuilders.post(Constant.FOLDER_PREFIX.getValue())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());

        Mockito.verify(createFolderService, Mockito.times(0))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
        Mockito.verify(createFolderService, Mockito.times(0))
                .create(ArgumentMatchers.anyString(), ArgumentMatchers.anyList(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("폴더 이름 수정 - 성공")
    @WithAccount
    void updateFolder() throws Exception {
        long folderId = 1L;
        RequestFolderNameDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFolderNameDto.class);

        Mockito.doNothing()
                .when(updateFolderNameService)
                .update(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());

        mockMvc.perform(
                MockMvcRequestBuilders.patch(Constant.FOLDER_PREFIX.getValue() + "/" + folderId + "/name")
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(updateFolderNameService, Mockito.times(1))
                .update(ArgumentMatchers.anyLong(), ArgumentMatchers.anyString(), ArgumentMatchers.anyLong());
    }

    @Test
    @DisplayName("폴더 삭제 - 성공")
    @WithAccount
    void deleteFolder() throws Exception {
        long folderId = 1L;

        Mockito.doNothing()
                .when(deleteFolderService)
                .delete(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong());

        mockMvc.perform(
                MockMvcRequestBuilders.delete(Constant.FOLDER_PREFIX.getValue() + "/" + folderId)
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        Mockito.verify(deleteFolderService, Mockito.times(1))
                .delete(ArgumentMatchers.anyLong(), ArgumentMatchers.anyLong());
    }
}

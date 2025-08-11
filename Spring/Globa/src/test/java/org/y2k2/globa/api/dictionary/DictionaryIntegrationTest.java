package org.y2k2.globa.api.dictionary;

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
import org.y2k2.globa.application.dictionary.dto.response.ResponseDictionaryDto;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.dictionary.DictionaryFixture;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class DictionaryIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private RoleFixture roleFixture;
    @Autowired
    private UserFixture userFixture;
    @Autowired
    private UserRoleFixture userRoleFixture;
    @Autowired
    private DictionaryFixture dictionaryFixture;

    @BeforeEach
    void setup() {
        UserEntity myUser = userFixture.save(
                UserFixture.builder()
                        .name("Test User")
                        .build()
        );

        setSecurityContext(myUser);
    }

    @Test
    @DisplayName("단어 검색 목록 조회 - 성공")
    @WithAccount
    void getDictionary_Success() throws Exception {
        String keyword = "test";
        DictionaryEntity dictionary = dictionaryFixture.save(
                DictionaryFixture.builder()
                        .word("test")
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", keyword)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseDictionaryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDictionaryDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.dictionary())
                .hasSize(1)
                .extracting("word")
                .containsExactly(dictionary.getWord());
    }

    @Test
    @DisplayName("단어 검색 목록 조회 - 성공 (빈 결과)")
    @WithAccount
    void getDictionary_Empty_Success() throws Exception {
        String keyword = "nonexistent";

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", keyword)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseDictionaryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDictionaryDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.dictionary())
                .isEmpty();
    }

    @Test
    @DisplayName("단어 추가 - 성공 (ADMIN)")
    @WithAccount()
    void addDictionary_Admin_Success() throws Exception {
        UserEntity adminUser = userFixture.save(
                UserFixture.builder()
                        .name("Admin User")
                        .build()
        );

        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(adminUser)
                        .role(roleFixture.getEntity(UserRole.ADMIN))
                        .build()
        );

        setSecurityContext(adminUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // 단어가 추가되었는지 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", "감동")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseDictionaryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDictionaryDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.dictionary())
                .isNotEmpty()
                .extracting("word")
                .contains("감동");
    }

    @Test
    @DisplayName("단어 추가 - 성공 (EDITOR)")
    @WithAccount
    void addDictionary_Editor_Success() throws Exception {
        UserEntity editorUser = userFixture.save(
                UserFixture.builder()
                        .name("Editor User")
                        .build()
        );

        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(editorUser)
                        .role(roleFixture.getEntity(UserRole.EDITOR))
                        .build()
        );

        setSecurityContext(editorUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated());

        // 단어가 추가되었는지 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", "감동")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseDictionaryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseDictionaryDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.dictionary())
                .isNotEmpty()
                .extracting("word")
                .contains("감동");
    }

    @Test
    @DisplayName("단어 추가 - 실패 (VIEWER)")
    @WithAccount
    void addDictionary_Viewer_Failure() throws Exception {
        UserEntity viewerUser = userFixture.save(
                UserFixture.builder()
                        .name("Viewer User")
                        .build()
        );

        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(viewerUser)
                        .role(roleFixture.getEntity(UserRole.VIEWER))
                        .build()
        );

        setSecurityContext(viewerUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("단어 추가 - 실패 (PUBLIC_USER)")
    @WithAccount
    void addDictionary_PublicUser_Failure() throws Exception {
        UserEntity publicUser = userFixture.save(
                UserFixture.builder()
                        .name("Public User")
                        .build()
        );

        userRoleFixture.save(
                UserRoleFixture.builder()
                        .user(publicUser)
                        .role(roleFixture.getEntity(UserRole.USER))
                        .build()
        );

        setSecurityContext(publicUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}

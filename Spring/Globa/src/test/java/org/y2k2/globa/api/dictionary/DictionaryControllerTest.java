package org.y2k2.globa.api.dictionary;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import net.jqwik.api.Arbitraries;
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
import org.y2k2.globa.api.DictionaryController;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.application.dictionary.dto.response.ResponseDictionaryDto;
import org.y2k2.globa.application.dictionary.service.CreateDictionaryService;
import org.y2k2.globa.application.dictionary.service.GetDictionaryService;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;

import java.util.List;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = DictionaryController.class)
@AutoConfigureMockMvc(addFilters = false)
public class DictionaryControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetDictionaryService getDictionaryService;
    @MockBean
    private CreateDictionaryService createDictionaryService;

    @Test
    @DisplayName("단어 검색 목록 조회 - 성공")
    @WithAccount
    void getDictionaryTest() throws Exception {
        String keyword = "test";

        List<DictionaryDto> dictionaryDtos = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(DictionaryDto.class)
                .set("word", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("engWord", Arbitraries.strings().withCharRange('a', 'z').ofLength(5))
                .set("description", Arbitraries.strings().withCharRange('a', 'z').ofLength(60))
                .set("category", Arbitraries.strings().withCharRange('가', '힣').ofLength(2))
                .set("pronunciation", Arbitraries.strings().withCharRange('가', '힣').ofLength(5))
                .sampleList(10);

        Mockito
                .when(getDictionaryService.get(keyword))
                .thenReturn(new ResponseDictionaryDto(dictionaryDtos));

        mockMvc.perform(
                        MockMvcRequestBuilders.get("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", keyword)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("단어 생성 - 성공")
    @WithAccount
    void createDictionaryTest() throws Exception {
        Long userId = 1L;

        Mockito
                .doNothing()
                .when(createDictionaryService).create(userId);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/dictionary")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"));

        Mockito
                .verify(createDictionaryService, Mockito.times(1))
                .create(userId);
    }
}

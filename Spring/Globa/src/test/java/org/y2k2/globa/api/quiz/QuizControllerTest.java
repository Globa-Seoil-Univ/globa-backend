package org.y2k2.globa.api.quiz;

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
import org.y2k2.globa.api.QuizController;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizzesDto;
import org.y2k2.globa.application.quiz.service.GetQuizzesService;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = QuizController.class)
public class QuizControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GetQuizzesService getQuizzesService;

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공")
    @WithAccount
    void getQuizzes_Success() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                userId = 1L;

        ResponseQuizzesDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseQuizzesDto.class);

        Mockito
                .when(getQuizzesService.get(folderId, recordId, userId))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .get("/folder/{folderId}/record/{recordId}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito
                .verify(getQuizzesService, Mockito.times(1))
                .get(folderId, recordId, userId);
    }
}

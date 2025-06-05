package org.y2k2.globa.api.quizattempt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
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
import org.y2k2.globa.api.QuizAttemptController;
import org.y2k2.globa.api.QuizController;
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.application.quiz.service.GetQuizzesService;
import org.y2k2.globa.application.quizattemp.service.CreateQuizAttemptsService;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;

import java.util.List;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = QuizAttemptController.class)
public class QuizAttemptControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateQuizAttemptsService createQuizAttemptsService;

    @Test
    @DisplayName("퀴즈 시도 결과 생성 - 성공")
    @WithAccount
    void postQuizAttempt_Success() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                userId = 1L;

        RequestQuizDto requestQuizDto = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(RequestQuizDto.Quiz.class, ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestQuizDto.class);

        Mockito
                .doNothing()
                .when(createQuizAttemptsService)
                .create(
                        Mockito.eq(folderId),
                        Mockito.eq(recordId),
                        Mockito.any(RequestQuizDto.class),
                        Mockito.eq(userId)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestQuizDto))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/folder/" + folderId + "/record/" + recordId + "/quiz"));

        Mockito
                .verify(createQuizAttemptsService, Mockito.times(1))
                .create(
                        Mockito.eq(folderId),
                        Mockito.eq(recordId),
                        Mockito.any(RequestQuizDto.class),
                        Mockito.eq(userId)
                );
    }

    @Test
    @DisplayName("퀴즈 시도 결과 생성 - 성공 (빈 퀴즈 결과)")
    @WithAccount
    void postQuizAttempt_EmptyQuiz_Success() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                userId = 1L;

        RequestQuizDto requestQuizDto = new RequestQuizDto();
        requestQuizDto.setQuizzes(List.of());

        Mockito
                .doNothing()
                .when(createQuizAttemptsService)
                .create(
                        Mockito.eq(folderId),
                        Mockito.eq(recordId),
                        Mockito.any(RequestQuizDto.class),
                        Mockito.eq(userId)
                );

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestQuizDto))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/folder/" + folderId + "/record/" + recordId + "/quiz"));

        Mockito
                .verify(createQuizAttemptsService, Mockito.times(1))
                .create(
                        Mockito.eq(folderId),
                        Mockito.eq(recordId),
                        Mockito.any(RequestQuizDto.class),
                        Mockito.eq(userId)
                );
    }

    @Test
    @DisplayName("퀴즈 시도 결과 생성 - 실패 (잘못된 요청)")
    void postQuizAttempt_BadRequest() throws Exception {
        Long folderId = 1L,
                recordId = 1L;

        RequestQuizDto requestQuizDto = new RequestQuizDto();
        requestQuizDto.setQuizzes(null);

        mockMvc.perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestQuizDto))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

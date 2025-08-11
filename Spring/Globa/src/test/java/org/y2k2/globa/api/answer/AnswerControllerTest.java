package org.y2k2.globa.api.answer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
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
import org.y2k2.globa.api.AnswerController;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.answer.service.CreateAnswerService;
import org.y2k2.globa.application.answer.service.DeleteAnswerService;
import org.y2k2.globa.application.answer.service.UpdateAnswerService;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = AnswerController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AnswerControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CreateAnswerService createAnswerService;
    @MockBean
    private UpdateAnswerService updateAnswerService;
    @MockBean
    private DeleteAnswerService deleteAnswerService;

    @Test
    @DisplayName("답변 생성 - 성공")
    @WithAccount
    void createAnswer_Success() throws Exception {
        Long inquiryId = 1L,
                userId = 1L;

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        String location = "/inquiry/" + inquiryId;

        Mockito
                .doNothing()
                .when(createAnswerService)
                .create(inquiryId, request, userId);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", location))
                .andDo(result -> log.info("Response = {}", result.getResponse().getHeader("Location")));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (잘못된 요청)")
    void createAnswer_Failure_InvalidRequest() throws Exception {
        Long inquiryId = 1L;
        RequestAnswerDto request = new RequestAnswerDto(null, null);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("답변 수정 - 성공")
    @WithAccount
    void updateAnswer_Success() throws Exception {
        Long inquiryId = 1L,
                answerId = 1L,
                userId = 1L;

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .defaultNotNull(true)
                .build()
                .giveMeOne(RequestAnswerDto.class);

        Mockito
                .doNothing()
                .when(updateAnswerService)
                .update(inquiryId, answerId, request, userId);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("답변 수정 - 실패 (잘못된 요청)")
    @WithAccount
    void updateAnswer_Failure_InvalidRequest() throws Exception {
        Long inquiryId = 1L,
                answerId = 1L;
        RequestAnswerDto request = new RequestAnswerDto(null, null);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("답변 삭제 - 성공")
    @WithAccount
    void deleteAnswer_Success() throws Exception {
        long inquiryId = 1L,
                answerId = 1L,
                userId = 1L;

        Mockito
                .doNothing()
                .when(deleteAnswerService)
                .delete(inquiryId, answerId, userId);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }
}

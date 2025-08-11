package org.y2k2.globa.api.inquiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
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
import org.y2k2.globa.api.InquiryController;
import org.y2k2.globa.application.inquiry.dto.request.InquiryPaginationDto;
import org.y2k2.globa.application.inquiry.dto.request.RequestInquiryDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDto;
import org.y2k2.globa.application.inquiry.service.CreateInquiryService;
import org.y2k2.globa.application.inquiry.service.GetInquiresService;
import org.y2k2.globa.application.inquiry.service.GetInquiryService;
import org.y2k2.globa.common.type.InquirySort;
import org.y2k2.globa.common.util.jwt.JWT;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@AutoConfigureMockMvc(addFilters = false)
@WebMvcTest(controllers = InquiryController.class)
public class InquiryControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetInquiresService getInquiresService;
    @MockBean
    private GetInquiryService getInquiryService;
    @MockBean
    private CreateInquiryService createInquiryService;

    @Test
    @DisplayName("문의 목록 조회 - 성공 (최근)")
    @WithAccount
    void getInquiries_Success_Recent() throws Exception {
        ResponseInquiryDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseInquiryDto.class)
                .set("inquires.title", Arbitraries.strings().ofMaxLength(80))
                .sample();

        String sort = InquirySort.R.name();

        Mockito
                .when(getInquiresService.get(Mockito.any(InquiryPaginationDto.class), Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("page", "1")
                        .param("count", "10")
                        .param("sort", sort)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (해결된 문의)")
    @WithAccount
    void getInquiries_Success_Solved() throws Exception {
        ResponseInquiryDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseInquiryDto.class)
                .set("inquires.title", Arbitraries.strings().ofMaxLength(80))
                .set("inquires.isSolved", true)
                .sample();

        String sort = InquirySort.S.name();

        Mockito
                .when(getInquiresService.get(Mockito.any(InquiryPaginationDto.class), Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("page", "1")
                        .param("count", "10")
                        .param("sort", sort)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (미해결 문의)")
    @WithAccount
    void getInquiries_Success_Unsolved() throws Exception {
        ResponseInquiryDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(ResponseInquiryDto.class)
                .set("inquires.title", Arbitraries.strings().ofMaxLength(80))
                .set("inquires.isSolved", false)
                .sample();

        String sort = InquirySort.N.name();

        Mockito
                .when(getInquiresService.get(Mockito.any(InquiryPaginationDto.class), Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("page", "1")
                        .param("count", "10")
                        .param("sort", sort)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공")
    @WithAccount
    void getInquiry_Success() throws Exception {
        ResponseInquiryDetailDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseInquiryDetailDto.class);

        Mockito
                .when(getInquiryService.get(Mockito.anyLong(), Mockito.anyLong()))
                .thenReturn(response);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", 1L)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("문의 내역 생성 - 성공")
    @WithAccount
    void createInquiry_Success() throws Exception {
        Long createdInquiryId = 1L;

        RequestInquiryDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestInquiryDto.class);

        Mockito
                .when(createInquiryService.create(Mockito.any(RequestInquiryDto.class), Mockito.anyLong()))
                .thenReturn(createdInquiryId);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/inquiry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/inquiry/" + createdInquiryId));
    }
    
    @Test
    @DisplayName("문의 내역 생성 - 실패 (잘못된 요청)")
    @WithAccount
    void createInquiry_Failure_InvalidRequest() throws Exception {
        RequestInquiryDto request = new RequestInquiryDto(null, null);

        mockMvc.perform(
                        MockMvcRequestBuilders.post("/inquiry")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }
}

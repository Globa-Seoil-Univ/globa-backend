package org.y2k2.globa.api.inquiry;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
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
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.inquiry.dto.request.RequestInquiryDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.InquirySort;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.fixture.answer.AnswerFixture;
import org.y2k2.globa.fixture.inquiry.InquiryFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class InquiryIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private InquiryFixture inquiryFixture;
    @Autowired
    private AnswerFixture answerFixture;

    private UserEntity user;
    private UserEntity otherUser;
    private InquiryEntity unSolvedInquiry;
    private InquiryEntity solvedInquiry;
    private AnswerEntity answer;

    @BeforeEach
    void setUp() {
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
        unSolvedInquiry = inquiryFixture.save(
                InquiryFixture
                        .builder()
                        .user(user)
                        .isSolved(false)
                        .build()
        );
        solvedInquiry = inquiryFixture.save(
                InquiryFixture
                        .builder()
                        .user(user)
                        .isSolved(true)
                        .build()
        );
        answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(solvedInquiry)
                        .user(otherUser)
                        .build()
        );

        setSecurityContext(user);
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (최신)")
    @WithAccount
    void getInquiries_Success() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("sort", InquirySort.R.name())
                        .param("page", "1")
                        .param("count", "10")
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(2);

        Assertions
                .assertThat(response.inquires())
                .hasSize(2)
                .extracting("inquiryId")
                .as("최신 순으로 인해 나중에 추가된 solvedInquiry가 먼저 나와야 한다.")
                .containsExactly(solvedInquiry.getInquiryId(), unSolvedInquiry.getInquiryId());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (미해결된 문의)")
    @WithAccount
    void getInquiries_Success_Unsolved() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("sort", InquirySort.N.name())
                        .param("page", "1")
                        .param("count", "10")
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(1);

        Assertions
                .assertThat(response.inquires())
                .hasSize(1)
                .extracting("inquiryId")
                .as("미해결된 문의만 조회되므로 unSolvedInquiry가 나와야 한다.")
                .containsExactly(unSolvedInquiry.getInquiryId());
    }

    @Test
    @DisplayName("문의 목록 조회 - 성공 (해결된 문의)")
    @WithAccount
    void getInquiries_Success_Solved() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry")
                        .param("sort", InquirySort.S.name())
                        .param("page", "1")
                        .param("count", "10")
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(1);

        Assertions
                .assertThat(response.inquires())
                .hasSize(1)
                .extracting("inquiryId")
                .as("해결된 문의만 조회되므로 solvedInquiry가 나와야 한다.")
                .containsExactly(solvedInquiry.getInquiryId());
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공")
    @WithAccount
    void getInquiryDetail_Success() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", unSolvedInquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.title())
                .isEqualTo(unSolvedInquiry.getTitle());

        Assertions
                .assertThat(response.content())
                .isEqualTo(unSolvedInquiry.getContent());

        Assertions
                .assertThat(response.answer())
                .isNull();
    }

    @Test
    @DisplayName("문의 상세 조회 - 성공 (해결된 문의)")
    @WithAccount
    void getInquiryDetail_Success_Solved() throws Exception {
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", solvedInquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.title())
                .isEqualTo(solvedInquiry.getTitle());

        Assertions
                .assertThat(response.content())
                .isEqualTo(solvedInquiry.getContent());

        Assertions
                .assertThat(response.answer())
                .isNotNull()
                .extracting("title")
                .isEqualTo(answer.getTitle());
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (문의 X)")
    @WithAccount
    void getInquiryDetail_Fail_NotFoundInquiry() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", 999L)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode()));
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (작성자 불일치)")
    @WithAccount
    void getInquiryDetail_Fail_MismatchInquiryOwner() throws Exception {
        setSecurityContext(otherUser);

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", unSolvedInquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_INQUIRY_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("문의 상세 조회 - 실패 (답변 X)")
    @WithAccount
    void getInquiryDetail_Fail_NotFoundAnswer() throws Exception {
        // 운영 이슈
        InquiryEntity errorUnSolvedInquiry = inquiryFixture.save(
                InquiryFixture
                        .builder()
                        .user(user)
                        .isSolved(true)
                        .build()
        );

        mockMvc.perform(
                MockMvcRequestBuilders.get("/inquiry/{inquiryId}", errorUnSolvedInquiry.getInquiryId())
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_ANSWER.getErrorCode()));
    }

    @Test
    @DisplayName("문의 등록 - 성공")
    @WithAccount
    void addInquiry_Success() throws Exception {
        RequestInquiryDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestInquiryDto.class);

        MvcResult createdResult = mockMvc.perform(
                MockMvcRequestBuilders.post("/inquiry")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andReturn();

        String location = createdResult.getResponse().getHeader("Location");

        log.info("Location = {}", location);

        Assertions
                .assertThat(location)
                .isNotNull()
                .startsWith("/inquiry/");

        // 추가된 문의가 있는지 확인
        MvcResult retrievedResult = mockMvc.perform(
                MockMvcRequestBuilders.get(location)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                retrievedResult.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        log.info("response =  {}", response);

        Assertions
                .assertThat(response.title())
                .isEqualTo(request.title());
    }

    @Test
    @DisplayName("문의 등록 - 실패 (잘못된 요청)")
    @WithAccount
    void addInquiry_Fail_BadRequest() throws Exception {
        RequestInquiryDto request = new RequestInquiryDto(null, null);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/inquiry")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("문의 등록 - 실패 (삭제된 사용자)")
    @WithAccount
    void addInquiry_Fail_DeletedUser() throws Exception {
        UserEntity deletedUser = userFixture.save(
                UserFixture
                        .builder()
                        .isDeleted(true)
                        .build()
        );

        setSecurityContext(deletedUser);

        RequestInquiryDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestInquiryDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders.post("/inquiry")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.DELETED_USER.getErrorCode()));
    }
}

package org.y2k2.globa.api.answer;

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
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.fixture.answer.AnswerFixture;
import org.y2k2.globa.fixture.inquiry.InquiryFixture;
import org.y2k2.globa.fixture.role.RoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.fixture.userrole.UserRoleFixture;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
public class AnswerIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private RoleFixture roleFixture;
    @Autowired
    private UserRoleFixture userRoleFixture;
    @Autowired
    private InquiryFixture inquiryFixture;
    @Autowired
    private AnswerFixture answerFixture;

    private UserEntity user;
    private UserEntity otherUser;
    private InquiryEntity inquiry;
    private RoleEntity admin;
    private RoleEntity editor;
    private RoleEntity viewer;
    private RoleEntity publicUser;

    @BeforeEach
    void setUp() {
        user = userFixture.save(
                UserFixture
                        .builder()
                        .build()
        );
        admin = roleFixture.getEntity(UserRole.ADMIN);
        editor = roleFixture.getEntity(UserRole.EDITOR);
        viewer = roleFixture.getEntity(UserRole.VIEWER);
        publicUser = roleFixture.getEntity(UserRole.USER);

        otherUser = userFixture.save(
                UserFixture
                        .builder()
                        .name("Other User")
                        .build()
        );
        inquiry = inquiryFixture.save(
                InquiryFixture
                        .builder()
                        .user(user)
                        .isSolved(false)
                        .build()
        );

        setSecurityContext(user);
    }

    @Test
    @DisplayName("답변 생성 - 성공 (Admin)")
    @WithAccount
    void createAnswer_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andDo(result -> {
                    log.info("Response location = {}", result.getResponse().getHeader("Location"));
                });
    }

    @Test
    @DisplayName("답변 생성 - 성공 (Editor)")
    @WithAccount
    void createAnswer_Success_Editor() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(editor)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"))
                .andDo(result -> {
                    log.info("Response location = {}", result.getResponse().getHeader("Location"));
                });
    }

    @Test
    @DisplayName("답변 생성 - 실패 (Viewer)")
    @WithAccount
    void createAnswer_Failure_Viewer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(viewer)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (Public User)")
    @WithAccount
    void createAnswer_Failure_PublicUser() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(publicUser)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (문의 X)")
    @WithAccount
    void createAnswer_Failure_NotFoundInquiry() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = 999L; // 존재하지 않는 문의 ID

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode()));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (답변 중복)")
    @WithAccount
    void createAnswer_Failure_AnswerDuplicated() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();

        // 이미 답변이 있는 경우
        inquiry.setIsSolved(true);
        inquiryFixture.save(inquiry);

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.INQUIRY_ANSWER_DUPLICATED.getErrorCode()));
    }

    @Test
    @DisplayName("답변 생성 - 실패 (잘못된 요청)")
    @WithAccount
    void createAnswer_Failure_BadRequest() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId();
        RequestAnswerDto request = new RequestAnswerDto(null, null);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .post("/inquiry/{inquiryId}/answer", inquiryId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("답변 수정 - 성공 (Admin)")
    @WithAccount
    void updateAnswer_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 바뀌었는지 확인
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/inquiry/{inquiryId}", inquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        Assertions
                .assertThat(response.answer().title())
                .isEqualTo(answer.getTitle());

        Assertions
                .assertThat(response.answer().content())
                .isEqualTo(request.content());
    }

    @Test
    @DisplayName("답변 수정 - 성공 (Editor)")
    @WithAccount
    void updateAnswer_Success_Editor() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(editor)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 바뀌었는지 확인
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/inquiry/{inquiryId}", inquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        Assertions
                .assertThat(response.answer().title())
                .isEqualTo(answer.getTitle());

        Assertions
                .assertThat(response.answer().content())
                .isEqualTo(request.content());
    }

    @Test
    @DisplayName("답변 수정 - 실패 (Viewer)")
    @WithAccount
    void updateAnswer_Failure_Viewer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(viewer)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (Public User)")
    @WithAccount
    void updateAnswer_Failure_PublicUser() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(publicUser)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (문의 X)")
    @WithAccount
    void updateAnswer_Failure_NotFoundInquiry() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = 999L, // 존재하지 않는 문의 ID
                answerId = answer.getAnswerId();

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode()));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (답변 X)")
    @WithAccount
    void updateAnswer_Failure_NotFoundAnswer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = 999L; // 존재하지 않는 답변 ID

        RequestAnswerDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestAnswerDto.class);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_ANSWER.getErrorCode()));
    }

    @Test
    @DisplayName("답변 수정 - 실패 (잘못된 요청)")
    @WithAccount
    void updateAnswer_Failure_BadRequest() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        RequestAnswerDto request = new RequestAnswerDto(null, null);

        mockMvc.perform(
                MockMvcRequestBuilders
                        .patch("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        )
                .andExpect(MockMvcResultMatchers.status().isBadRequest());
    }

    @Test
    @DisplayName("답변 삭제 - 성공 (Admin)")
    @WithAccount
    void deleteAnswer_Success() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 답변이 삭제되었는지 확인
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/inquiry/{inquiryId}", inquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        Assertions
                .assertThat(response.answer())
                .isNull(); // 답변이 삭제되었으므로 null이어야 함
    }

    @Test
    @DisplayName("답변 삭제 - 성공 (Editor)")
    @WithAccount
    void deleteAnswer_Success_Editor() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(editor)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 답변이 삭제되었는지 확인
        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders
                        .get("/inquiry/{inquiryId}", inquiry.getInquiryId())
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseInquiryDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseInquiryDetailDto.class
        );

        Assertions
                .assertThat(response.answer())
                .isNull(); // 답변이 삭제되었으므로 null이어야 함
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (Viewer)")
    @WithAccount
    void deleteAnswer_Failure_Viewer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(viewer)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (Public User)")
    @WithAccount
    void deleteAnswer_Failure_PublicUser() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(publicUser)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = answer.getAnswerId();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_PERMISSION.getErrorCode()));
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (문의 X)")
    @WithAccount
    void deleteAnswer_Failure_NotFoundInquiry() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        AnswerEntity answer = answerFixture.save(
                AnswerFixture
                        .builder()
                        .inquiry(inquiry)
                        .user(user)
                        .build()
        );

        Long inquiryId = 999L, // 존재하지 않는 문의 ID
                answerId = answer.getAnswerId();

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_INQUIRY.getErrorCode()));
    }

    @Test
    @DisplayName("답변 삭제 - 실패 (답변 X)")
    @WithAccount
    void deleteAnswer_Failure_NotFoundAnswer() throws Exception {
        userRoleFixture.save(
                UserRoleFixture
                        .builder()
                        .user(user)
                        .role(admin)
                        .build()
        );

        Long inquiryId = inquiry.getInquiryId(),
                answerId = 999L; // 존재하지 않는 답변 ID

        mockMvc.perform(
                MockMvcRequestBuilders
                        .delete("/inquiry/{inquiryId}/answer/{answerId}", inquiryId, answerId)
                        .accept(MediaType.APPLICATION_JSON)
        )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_ANSWER.getErrorCode()));
    }
}

package org.y2k2.globa.api.quizattempt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizzesDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.quiz.QuizFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
public class QuizAttemptIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private QuizFixture quizFixture;

    private UserEntity myUser;
    private UserEntity otherUser;
    private FolderEntity myFolder;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private RecordEntity record;
    private QuizEntity quiz;

    @BeforeEach
    void setup() {
        myUser = userFixture.save(
                UserFixture.builder()
                        .build()
        );
        otherUser = userFixture.save(
                UserFixture.builder()
                        .name("Other User")
                        .build()
        );

        myFolder = folderFixture.save(
                FolderFixture.builder()
                        .user(myUser)
                        .build()
        );

        FolderRoleEntity owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        editor = folderRoleFixture.getEntity(FolderRole.EDITOR);
        reader = folderRoleFixture.getEntity(FolderRole.READER);

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(myUser)
                        .role(owner)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        record = recordFixture.save(
                RecordFixture.builder()
                        .folder(myFolder)
                        .user(myUser)
                        .build()
        );

        quiz = quizFixture.save(
                QuizFixture.builder()
                        .record(record)
                        .build()
        );

        setSecurityContext(myUser);
    }

    private void verifyGetAnalysis() throws Exception {
        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .get("/user/analysis")
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.USER_PREFIX.getValue() + "/analysis")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andDo(MockMvcResultHandlers.print())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseAnalysisDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.quizGrades())
                .as("퀴즈 결과가 생성되었는지 확인합니다.")
                .isNotNull()
                .isNotEmpty();

        Assertions
                .assertThat(response.quizGrades())
                .as("퀴즈 결과과 올바른지 확인합니다.")
                .allSatisfy(quizGrade -> {
                    Assertions
                            .assertThat(quizGrade.quizGrade())
                            .as("총 1개의 퀴즈에서 정답을 맞췄으므로, 퀴즈 결과는 100점이어야 합니다.")
                            .isEqualTo(100);
                });
    }

    @Test
    @DisplayName("퀴즈 결과 생성 - 성공 (Owner)")
    @WithAccount
    void createQuizAttempt_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", quiz.getQuizId())
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        String location = "/user/analysis";

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", location));

        verifyGetAnalysis();
    }

    @Test
    @DisplayName("퀴즈 결과 생성 - 성공 (Editor)")
    @WithAccount
    void createQuizAttempt_Success_Editor() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", quiz.getQuizId())
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(otherUser)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        setSecurityContext(otherUser);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/user/analysis"));

        verifyGetAnalysis();
    }

    @Test
    @DisplayName("퀴즈 결과 생성 - 성공 (Reader)")
    @WithAccount
    void createQuizAttempt_Success_Reader() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", quiz.getQuizId())
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(otherUser)
                        .role(reader)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        setSecurityContext(otherUser);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", "/user/analysis"));

        verifyGetAnalysis();
    }




    @Test
    @DisplayName("퀴즈 결과 생성 - 실패 (권한 X)")
    @WithAccount
    void createQuizAttempt_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", quiz.getQuizId())
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        setSecurityContext(otherUser);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode()));
    }

    @Test
    @DisplayName("퀴즈 결과 생성 - 실패 (문서 X)")
    @WithAccount
    void createQuizAttempt_Fail_NoRecord() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = 999L; // 존재하지 않는 문서 ID

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", quiz.getQuizId())
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_RECORD.getErrorCode()));
    }

    @Test
    @DisplayName("퀴즈 결과 생성 - 실패 (퀴즈 ID 불일치)")
    @WithAccount
    void createQuizAttempt_Fail_MismatchQuizRecordId() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        RequestQuizDto.Quiz quizDto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeBuilder(RequestQuizDto.Quiz.class)
                .set("quizId", 999L) // 존재하지 않는 퀴즈 ID
                .set("isCorrect", true)
                .sample();

        RequestQuizDto request = new RequestQuizDto();
        request.setQuizzes(List.of(quizDto));

        log.info("request = {}", request);

        mockMvc
                .perform(
                        MockMvcRequestBuilders
                                .post("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header("Authorization", jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_QUIZ_RECORD_ID.getErrorCode()));
    }
}

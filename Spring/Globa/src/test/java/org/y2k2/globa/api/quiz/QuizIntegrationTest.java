package org.y2k2.globa.api.quiz;

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
import org.y2k2.globa.application.quiz.dto.common.QuizDto;
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

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

@Slf4j
public class QuizIntegrationTest extends IntegrationTest {
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
    private FolderEntity myFolder;
    private UserEntity otherUser;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private RecordEntity record;

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

        setSecurityContext(myUser);
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공")
    @WithAccount
    void getQuizzes_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        String[] questions = {
                "Is the capital of South Korea Seoul?",
                "Is the capital of France Paris?",
                "Is the capital of Japan Tokyo?",
                "Is the capital of the United States Washington, D.C.?",
                "Is the capital of Germany Moskva?"
        };
        boolean[] answers = {true, true, true, true, false};

        List<QuizEntity> quizzes = Arrays.stream(questions)
                .map(question -> quizFixture.save(
                        QuizFixture.builder()
                                .question(question)
                                .answer(answers[Arrays.asList(questions).indexOf(question)])
                                .record(record)
                                .build()
                ))
                .toList();

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseQuizzesDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseQuizzesDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.quizzes())
                .as("퀴즈 목록은 정상적으로 조회되어야 합니다.")
                .hasSize(quizzes.size());

        IntStream.range(0, response.quizzes().size())
                .forEach(i -> {
                    QuizDto quiz = response.quizzes().get(i);
                    Assertions.assertThat(quiz.question()).isEqualTo(questions[i]);
                    Assertions.assertThat(quiz.answer()).isEqualTo(answers[i]);
                });
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공 (빈 목록)")
    @WithAccount
    void getQuizzes_EmptyList() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseQuizzesDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseQuizzesDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.quizzes())
                .as("퀴즈 목록은 비어 있어야 합니다.")
                .isEmpty();
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공 (Editor)")
    @WithAccount
    void getQuizzes_Success_Editor() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(otherUser)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        String[] questions = {
                "Is the capital of South Korea Seoul?",
                "Is the capital of France Paris?"
        };

        List<QuizEntity> quizzes = Arrays.stream(questions)
                .map(question -> quizFixture.save(
                        QuizFixture.builder()
                                .question(question)
                                .answer(true)
                                .record(record)
                                .build()
                ))
                .toList();

        setSecurityContext(otherUser);

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseQuizzesDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseQuizzesDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.quizzes())
                .as("퀴즈 목록은 정상적으로 조회되어야 합니다.")
                .hasSize(quizzes.size());

        IntStream.range(0, response.quizzes().size())
                .forEach(i -> {
                    QuizDto quiz = response.quizzes().get(i);
                    Assertions.assertThat(quiz.question()).isEqualTo(questions[i]);
                    Assertions.assertThat(quiz.answer()).isEqualTo(true);
                });
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 성공 (Reader)")
    @WithAccount
    void getQuizzes_Success_Reader() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        folderShareFixture.save(
                FolderShareFixture.builder()
                        .folder(myFolder)
                        .owner(myUser)
                        .target(otherUser)
                        .role(reader)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        String[] questions = {
                "Is the capital of South Korea Seoul?",
                "Is the capital of France Paris?"
        };

        List<QuizEntity> quizzes = Arrays.stream(questions)
                .map(question -> quizFixture.save(
                        QuizFixture.builder()
                                .question(question)
                                .answer(true)
                                .record(record)
                                .build()
                ))
                .toList();

        setSecurityContext(otherUser);

        MvcResult result = mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseQuizzesDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseQuizzesDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.quizzes())
                .as("퀴즈 목록은 정상적으로 조회되어야 합니다.")
                .hasSize(quizzes.size());

        IntStream.range(0, response.quizzes().size())
                .forEach(i -> {
                    QuizDto quiz = response.quizzes().get(i);
                    Assertions.assertThat(quiz.question()).isEqualTo(questions[i]);
                    Assertions.assertThat(quiz.answer()).isEqualTo(true);
                });
    }

    @Test
    @DisplayName("퀴즈 목록 조회 - 실패 (권한 X)")
    @WithAccount
    void getQuizzes_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = record.getRecordId();

        setSecurityContext(otherUser);

        mockMvc
                .perform(
                        MockMvcRequestBuilders.get("/folder/{folder_id}/record/{record_id}/quiz", folderId, recordId)
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode()));
    }
}

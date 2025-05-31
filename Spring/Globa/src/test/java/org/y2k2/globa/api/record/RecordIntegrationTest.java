package org.y2k2.globa.api.record;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.kafka.dto.request.RequestKafkaDto;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
import org.y2k2.globa.application.record.dto.request.RequestRecordNameDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
import org.y2k2.globa.application.study.dto.request.RequestStudyDto;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.KafkaProducer;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.analysis.AnalysisFixture;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.highlight.HighlightFixture;
import org.y2k2.globa.fixture.keyword.KeywordFixture;
import org.y2k2.globa.fixture.quiz.QuizFixture;
import org.y2k2.globa.fixture.quizattempt.QuizAttemptFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.study.StudyFixture;
import org.y2k2.globa.fixture.summary.SummaryFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.study.entity.StudyEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
public class RecordIntegrationTest extends IntegrationTest {
    @Autowired
    private CacheManager cacheManager;
    @Autowired
    private JWT jwt;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserFixture userFixture;
    @Autowired
    private FolderRoleFixture folderRoleFixture;
    @Autowired
    private FolderFixture folderFixture;
    @Autowired
    private FolderShareFixture folderShareFixture;
    @Autowired
    private RecordFixture recordFixture;
    @Autowired
    private SummaryFixture summaryFixture;
    @Autowired
    private AnalysisFixture analysisFixture;
    @Autowired
    private HighlightFixture highlightFixture;
    @Autowired
    private SectionFixture sectionFixture;
    @Autowired
    private StudyFixture studyFixture;
    @Autowired
    private QuizFixture quizFixture;
    @Autowired
    private QuizAttemptFixture quizAttemptFixture;
    @Autowired
    private KeywordFixture keywordFixture;

    @MockBean
    private KafkaProducer kafkaProducer;
    @MockBean
    private FileStore fileStore;

    private UserEntity user;
    private UserEntity otherUser;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private FolderEntity myFolder;
    private RecordEntity myRecord;
    private FolderEntity otherFolder;
    private RecordEntity otherRecord;

    @BeforeEach
    public void setUp() {
        Optional.ofNullable(cacheManager.getCache("aggregateRecord")).ifPresent(Cache::clear);

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
        owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        editor = folderRoleFixture.getEntity(FolderRole.EDITOR);
        reader = folderRoleFixture.getEntity(FolderRole.READER);

        myFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .build()
        );
        myRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(myFolder)
                        .path("/1/myrecord.ogg")
                        .build()
        );

        otherFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(otherUser)
                        .build()
        );
        otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(otherFolder)
                        .path("/1/otherrecord.ogg")
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(user)
                        .folder(myFolder)
                        .role(owner)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .build()
        );

        setSecurityContext(user);
    }

    @AfterEach
    void tearDown() {
        Optional.ofNullable(cacheManager.getCache("aggregateRecord")).ifPresent(Cache::clear);
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 성공 (내 폴더 O)")
    @WithAccount
    void getRecordsInFolder() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .build()
        );
        RecordEntity myOtherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(myFolder)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(editor)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), myFolder.getFolderId())
                        .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                        .accept(MediaType.APPLICATION_JSON)
                        .param("page", "1")
                        .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsByFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsByFolderDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 3개의 문서가 조회되어야 합니다.")
                .isEqualTo(3);

        Assertions.assertThat(response.isOwner())
                .as("내가 소유한 폴더의 문서 조회이므로 true여야 합니다.")
                .isTrue();

        Assertions.assertThat(response.records())
                .as("내 폴더에 속한 모든 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                    Assertions.assertThat(record.recordId())
                            .isIn(myRecord.getRecordId(), myOtherRecord.getRecordId(), otherRecord.getRecordId())
                );
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 성공 (내 폴더 X)")
    @WithAccount
    void getRecordsInFolderNotMyFolder() throws Exception {
        RecordEntity myRecordInOtherFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(editor)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), otherFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsByFolderDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsByFolderDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 2개의 문서가 조회되어야 합니다.")
                .isEqualTo(2);

        Assertions.assertThat(response.isOwner())
                .as("내가 소유한 폴더가 아니므로, false여야 합니다.")
                .isFalse();

        Assertions.assertThat(response.records())
                .as("내 폴더에 속한 모든 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                        Assertions.assertThat(record.recordId())
                                .isIn(myRecordInOtherFolder.getRecordId(), otherRecord.getRecordId())
                );
    }

    @Test
    @DisplayName("폴더 내 문서 조회 - 실패 (내 폴더 X, 권한 X)")
    @WithAccount
    void getRecordsInFolderNotMyFolderWithoutPermission() throws Exception {
        recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );
        recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(otherFolder)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.get(Constant.RECORD_PREFIX.getValue(), otherFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("최근 문서 조회 - 성공")
    @WithAccount
    void getRecentRecords() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/recent")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 2개의 문서가 조회되어야 합니다.")
                .isEqualTo(2);

        Assertions.assertThat(response.records())
                .as("내가 소유한 폴더의 문서와 공유된 폴더의 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                        Assertions.assertThat(record.recordId())
                                .isIn(myRecord.getRecordId(), otherRecord.getRecordId())
                );
    }

    @Test
    @DisplayName("최근 문서 조회 - 성공 (외부 사용자 폴더에 대한 권한 X)")
    @WithAccount
    void getRecentRecordsWithoutPermission() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/recent")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 1개의 문서가 조회되어야 합니다.")
                .isEqualTo(1);

        Assertions.assertThat(response.records().get(0).recordId())
                .as("내가 소유한 폴더의 문서만 조회되어야 합니다.")
                .isEqualTo(myRecord.getRecordId());
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공")
    @WithAccount
    void getRecord() throws Exception {
        String[] sectionTitles = {"section title 1", "section title 2"};
        String[] summaries = {"summary content 1", "summary content 2", "summary content 3"};
        String[] analyses = {"analysis content 1", "analysis content 2"};

        SectionEntity firstSection = sectionFixture.save(
                SectionFixture
                        .builder()
                        .record(myRecord)
                        .title(sectionTitles[0])
                        .startTime(0L)
                        .endTime(10L)
                        .build()
        );
        SectionEntity secondSection = sectionFixture.save(
                SectionFixture
                        .builder()
                        .record(myRecord)
                        .title(sectionTitles[1])
                        .startTime(11L)
                        .endTime(20L)
                        .build()
        );
        summaryFixture.save(
                SummaryFixture
                        .builder()
                        .section(firstSection)
                        .content(summaries[0])
                        .build()
        );
        summaryFixture.save(
                SummaryFixture
                        .builder()
                        .section(firstSection)
                        .content(summaries[1])
                        .build()
        );
        summaryFixture.save(
                SummaryFixture
                        .builder()
                        .section(secondSection)
                        .content(summaries[2])
                        .build()
        );

        analysisFixture.save(
                AnalysisFixture
                        .builder()
                        .section(firstSection)
                        .content(analyses[0])
                        .build()
        );
        analysisFixture.save(
                AnalysisFixture
                        .builder()
                        .section(secondSection)
                        .content(analyses[1])
                        .build()
        );

        HighlightEntity highlight1 = highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(firstSection)
                        .startIndex(0L)
                        .endIndex(5L)
                        .build()
        );
        HighlightEntity highlight2 = highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(secondSection)
                        .startIndex(10L)
                        .endIndex(15L)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.recordId())
                .as("조회한 문서의 ID가 일치해야 합니다.")
                .isEqualTo(myRecord.getRecordId());

        Assertions.assertThat(response.title())
                .as("조회한 문서의 제목이 일치해야 합니다.")
                .isEqualTo(myRecord.getTitle());

        Assertions.assertThat(response.folder().title())
                .as("조회한 문서의 폴더 제목이 일치해야 합니다.")
                .isEqualTo(myFolder.getTitle());

        Assertions.assertThat(response.sections())
                .as("조회한 문서의 섹션 개수가 일치해야 합니다.")
                .hasSize(2);

        Assertions.assertThat(response.sections())
                .as("조회한 문서의 섹션")
                .hasSize(2)
                .satisfies(sections -> {
                    // 첫 번째 섹션 검증
                    Assertions.assertThat(sections.get(0))
                            .satisfies(section -> {
                                Assertions.assertThat(section.title()).isEqualTo(sectionTitles[0]);
                                Assertions.assertThat(section.summaries()).hasSize(2);
                                Assertions.assertThat(section.summaries().get(0).content()).isEqualTo(summaries[0]);
                                Assertions.assertThat(section.summaries().get(1).content()).isEqualTo(summaries[1]);
                                Assertions.assertThat(section.analyses().highlights().get(0).highlightId())
                                        .isEqualTo(highlight1.getHighlightId());
                            });

                    // 두 번째 섹션 검증
                    Assertions.assertThat(sections.get(1))
                            .satisfies(section -> {
                                Assertions.assertThat(section.title()).isEqualTo(sectionTitles[1]);
                                Assertions.assertThat(section.summaries()).hasSize(1);
                                Assertions.assertThat(section.summaries().get(0).content()).isEqualTo(summaries[2]);
                                Assertions.assertThat(section.analyses().highlights().get(0).highlightId())
                                        .isEqualTo(highlight2.getHighlightId());
                            });
                });
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공 (공유된 폴더의 문서)")
    @WithAccount
    void getRecordInSharedFolder() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(editor)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                otherFolder.getFolderId(),
                                otherRecord.getRecordId()
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.recordId())
                .as("조회한 문서의 ID가 일치해야 합니다.")
                .isEqualTo(otherRecord.getRecordId());
    }

    @Test
    @DisplayName("문서 상세 조회 - 성공 (링크 공유)")
    @WithAccount
    void getRecordWithLinkShare() throws Exception {
        RecordEntity sharedRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(otherFolder)
                        .isShare(true) // 링크 공유 설정
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                otherFolder.getFolderId(),
                                sharedRecord.getRecordId()
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.recordId())
                .as("조회한 문서의 ID가 일치해야 합니다.")
                .isEqualTo(sharedRecord.getRecordId());
    }

    @Test
    @DisplayName("문서 상세 조회 - 실패 (문서 X)")
    @WithAccount
    void getRecordNotFound() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                myFolder.getFolderId(),
                                999L // 존재하지 않는 문서 ID
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("문서 상세 조회 - 실패 (권한 X)")
    @WithAccount
    void getRecordWithoutPermission() throws Exception {
        FolderEntity otherFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(otherUser)
                        .build()
        );
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(otherFolder)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                otherFolder.getFolderId(),
                                otherRecord.getRecordId()
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("문서 시각화 자료 조회 - 성공")
    @WithAccount
    void getAnalysis() throws Exception {
        String[] keywords = {"keyword1", "keyword2"};

        // 오늘 기준 스터디 기록 1개 생성
        StudyEntity study = studyFixture.save(
                StudyFixture
                        .builder()
                        .user(user)
                        .record(myRecord)
                        .build()
        );

        QuizEntity quiz1 = quizFixture.save(
                QuizFixture
                        .builder()
                        .record(myRecord)
                        .build()
        );
        QuizEntity quiz2 = quizFixture.save(
                QuizFixture
                        .builder()
                        .record(myRecord)
                        .build()
        );

        // 어제 기준 퀴즈 시도 기록 1개 생성
        QuizAttemptEntity yesterdayQuiz = quizAttemptFixture.save(
                QuizAttemptFixture
                        .builder()
                        .user(user)
                        .quiz(quiz1)
                        .isCorrect(true)
                        .build()
        );

        yesterdayQuiz.setCreatedTime(new CustomTimestamp().getTimestamp().minusDays(1));
        quizAttemptFixture.save(yesterdayQuiz);

        // 오늘 기준 퀴즈 시도 기록 2개 생성
        quizAttemptFixture.save(
                QuizAttemptFixture
                        .builder()
                        .user(user)
                        .quiz(quiz1)
                        .isCorrect(true)
                        .build()
        );
        quizAttemptFixture.save(
                QuizAttemptFixture
                        .builder()
                        .user(user)
                        .quiz(quiz2)
                        .isCorrect(false)
                        .build()
        );

        keywordFixture.save(
                KeywordFixture
                        .builder()
                        .record(myRecord)
                        .word(keywords[0])
                        .build()
        );
        keywordFixture.save(
                KeywordFixture
                        .builder()
                        .record(myRecord)
                        .word(keywords[1])
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis",
                                myFolder.getFolderId(),
                                myRecord.getRecordId()
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseAnalysisDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.keywords())
                .as("문서에 대한 키워드가 조회되어야 합니다.")
                .hasSize(2)
                .allSatisfy(keyword ->
                        Assertions.assertThat(keyword.word())
                                .isIn(keywords[0], keywords[1])
                );

        Assertions.assertThat(response.studyTimes())
                .as("문서에 대한 스터디 기록이 조회되어야 합니다.")
                .hasSize(1)
                .allSatisfy(s ->
                        Assertions.assertThat(s.studyTime())
                                .isEqualTo(study.getStudyTime())
                );

        Assertions.assertThat(response.quizGrades())
                .as("문서에 대한 퀴즈 시도 기록이 조회되어야 합니다.")
                .hasSize(2)
                .allSatisfy(quizGrade -> {
                    Assertions.assertThat(quizGrade.quizGrade())
                            .isBetween(0.0, 100.0);
                    Assertions.assertThat(quizGrade.createdTime())
                            .isNotNull();
                });
    }

    @Test
    @DisplayName("문서 시각화 자료 조회 - 실패 (문서 X)")
    @WithAccount
    void getAnalysisNotFound() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis",
                                myFolder.getFolderId(),
                                999L // 존재하지 않는 문서 ID
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();
    }

    @Test
    @DisplayName("문서 시각화 자료 조회 - 실패 (권한 X)")
    @WithAccount
    void getAnalysisWithoutPermission() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis",
                                otherFolder.getFolderId(),
                                otherRecord.getRecordId()
                        )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andReturn();
    }

    @Test
    @DisplayName("문서 검색 - 성공")
    @WithAccount
    void searchRecords() throws Exception {
        String keyword = "test";

        recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(myFolder)
                        .title("test title 1")
                        .build()
        );
        recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(myFolder)
                        .title("test title 2")
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", keyword)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordSearchDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordSearchDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.getTotal())
                .as("총 2개의 문서가 조회되어야 합니다.")
                .isEqualTo(2);

        Assertions.assertThat(response.getRecords())
                .as("제목에 'test'가 포함된 문서가 조회되어야 합니다.")
                .allSatisfy(record ->
                        Assertions.assertThat(record.getTitle())
                                .contains(keyword)
                );
    }

    @Test
    @DisplayName("문서 검색 - 성공 (검색어 X)")
    @WithAccount
    void searchRecordsWithoutKeyword() throws Exception {
        String keyword = "";

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/search")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("keyword", keyword)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordSearchDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordSearchDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.getTotal())
                .as("총 1개의 문서가 조회되어야 합니다.")
                .isEqualTo(1);

        Assertions.assertThat(response.getRecords().get(0).getRecordId())
                .as("접근 가능한 모든 문서를 검색합니다.")
                .isEqualTo(myRecord.getRecordId());
    }

    @Test
    @DisplayName("공유 받는 문서 조회 - 성공")
    @WithAccount
    void getReceivingRecord() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/receiving")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 1개의 문서가 조회되어야 합니다.")
                .isEqualTo(1);

        Assertions.assertThat(response.records())
                .as("공유 받은 문서가 조회되어야 합니다.")
                .allSatisfy(record -> {
                        Assertions.assertThat(record.recordId())
                                .isEqualTo(otherRecord.getRecordId());

                        Assertions.assertThat(record.folderId())
                                .isEqualTo(otherFolder.getFolderId());
                    }
                );
    }

    @Test
    @DisplayName("공유 받는 문서 조회 - 성공 (공유된 폴더 X)")
    @WithAccount
    void getReceivingRecordWithoutSharedFolder() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/receiving")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 0개의 문서가 조회되어야 합니다.")
                .isEqualTo(0);

        Assertions.assertThat(response.records())
                .as("공유 받은 문서가 없어야 합니다.")
                .isEmpty();
    }

    @Test
    @DisplayName("공유 하는 문서 조회 - 성공")
    @WithAccount
    void getSharingRecord() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/sharing")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 1개의 문서가 조회되어야 합니다.")
                .isEqualTo(1);

        Assertions.assertThat(response.records())
                .as("공유 하는 문서가 조회되어야 합니다.")
                .allSatisfy(record -> {
                        Assertions.assertThat(record.recordId())
                                .isEqualTo(myRecord.getRecordId());

                        Assertions.assertThat(record.folderId())
                                .isEqualTo(myFolder.getFolderId());
                    }
                );
    }

    @Test
    @DisplayName("공유 하는 문서 조회 - 성공 (공유하는 폴더 X)")
    @WithAccount
    void getSharingRecordWithoutSharedFolder() throws Exception {
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get("/record/sharing")
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordsDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordsDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.total())
                .as("총 0개의 문서가 조회되어야 합니다.")
                .isEqualTo(0);

        Assertions.assertThat(response.records())
                .as("공유 하는 문서가 없어야 합니다.")
                .isEmpty();
    }

    @Test
    @DisplayName("문서 추가 - 성공")
    @WithAccount
    void createRecord() throws Exception {
        RequestPostRecordDto request = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        FileDto fileDto = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FileDto.class)
                .set("storePath", request.path())
                .sample();

        log.info("request = {}", request);
        log.info("fileDto = {}", fileDto);

        Mockito.when(fileStore.getFile(request.path()))
                .thenReturn(Optional.of(fileDto));

        Mockito.doNothing()
                .when(kafkaProducer)
                .send(Mockito.anyString(), Mockito.anyString(), Mockito.any(RequestKafkaDto.class));

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.RECORD_PREFIX.getValue(), myFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk());
    }

    @Test
    @DisplayName("문서 추가 - 실패 (폴더 X)")
    @WithAccount
    void createRecordNotFoundFolder() throws Exception {
        RequestPostRecordDto request = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.RECORD_PREFIX.getValue(), 999L) // 존재하지 않는 폴더 ID
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 추가 - 실패 (권한 X)")
    @WithAccount
    void createRecordWithoutPermission() throws Exception {
        RequestPostRecordDto request = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.RECORD_PREFIX.getValue(), otherFolder.getFolderId()) // 다른 사용자의 폴더 ID
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 추가 - 실패 (Firebase Storage 파일 X)")
    @WithAccount
    void createRecordNotFoundFile() throws Exception {
        RequestPostRecordDto request = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestPostRecordDto.class);

        log.info("request = {}", request);

        Mockito.when(fileStore.getFile(request.path()))
                .thenReturn(Optional.empty());

        mockMvc.perform(
                        MockMvcRequestBuilders.post(Constant.RECORD_PREFIX.getValue(), myFolder.getFolderId())
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 링크 공유 - 성공")
    @WithAccount
    void createRecordLinkShare() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.isShare())
                .as("문서 링크 공유가 성공적으로 이루어져야 합니다.")
                .isTrue();
    }

    @Test
    @DisplayName("문서 링크 공유 - 성공 (소유자 권한)")
    @WithAccount
    void createRecordLinkShareAsOwner() throws Exception {
        RecordEntity otherRecordInMyFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.isShare())
                .as("문서 링크 공유가 성공적으로 이루어져야 합니다.")
                .isTrue();
    }

    @Test
    @DisplayName("문서 링크 공유 - 실패 (폴더 X)")
    @WithAccount
    void createRecordLinkShareNotFoundFolder() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        999L,
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 링크 공유 - 실패 (업로드된 문서와 폴더 ID 불일치)")
    @WithAccount
    void createRecordLinkShareNotMyFolder() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 - 실패 (문서 소유 X)")
    @WithAccount
    void createRecordLinkShareNotMyRecord() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 - 실패 (다른 폴더에 있는 문서)")
    @WithAccount
    void createRecordLinkShareNotMatched() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .isShare(false)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 - 실패 (편집 권한 X)")
    @WithAccount
    void createRecordLinkShareWithoutPermission() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );

        // 나중에 reader 권한으로 변경된 경우
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 이름 수정 - 성공")
    @WithAccount
    void updateRecordTitle() throws Exception {
        RequestRecordNameDto request = new RequestRecordNameDto("new title");

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/name",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.title())
                .as("문서 제목이 수정되어야 합니다.")
                .isEqualTo(request.title());
    }

    @Test
    @DisplayName("문서 이름 수정 - 성공 (소유자 권한)")
    @WithAccount
    void updateRecordTitleAsOwner() throws Exception {
        RecordEntity otherRecordInMyFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .build()
        );

        RequestRecordNameDto request = new RequestRecordNameDto("new title");

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/name",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.title())
                .as("문서 제목이 수정되어야 합니다.")
                .isEqualTo(request.title());
    }

    @Test
    @DisplayName("문서 이름 수정 - 실패 (문서 X)")
    @WithAccount
    void updateRecordTitleNotFound() throws Exception {
        RequestRecordNameDto request = new RequestRecordNameDto("new title");

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/name",
                                        myFolder.getFolderId(),
                                        999L // 존재하지 않는 문서 ID
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 이름 수정 - 실패 (업로드된 문서와 폴더 ID 불일치)")
    @WithAccount
    void updateRecordTitleNotMyFolder() throws Exception {
        RequestRecordNameDto request = new RequestRecordNameDto("new title");

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/name",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 이름 수정 - 실패 (문서 소유 X)")
    @WithAccount
    void updateRecordTitleNotMyRecord() throws Exception {
        RequestRecordNameDto request = new RequestRecordNameDto("new title");

        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/name",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 이름 수정 - 실패 (편집 권한 X)")
    @WithAccount
    void updateRecordTitleWithoutPermission() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );

        // 나중에 reader 권한으로 변경된 경우
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 성공")
    @WithAccount
    void moveRecord() throws Exception {
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(user)
                        .folder(newMyFolder)
                        .role(owner)
                        .build()
        );

        String oldPath = myRecord.getPath();
        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        Mockito.doNothing()
                .when(fileStore)
                .moveFile(Mockito.anyString(), Mockito.anyString());

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(Mockito.anyString());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        newMyFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.recordId())
                .as("문서 ID가 일치해야 합니다.")
                .isEqualTo(myRecord.getRecordId());

        Assertions.assertThat(response.folder().title())
                .as("문서의 폴더 제목이 일치해야 합니다.")
                .isEqualTo(newMyFolder.getTitle());

        Assertions.assertThat(response.path())
                .as("문서의 경로가 변경되어야 합니다.")
                .isNotEqualTo(oldPath);
    }

    @Test
    @DisplayName("문서 폴더 이동 - 성공 (소유자 권한)")
    @WithAccount
    void moveRecordAsOwner() throws Exception {
        RecordEntity otherRecordInMyFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .build()
        );
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(user)
                        .target(user)
                        .folder(newMyFolder)
                        .role(owner)
                        .build()
        );

        String oldPath = otherRecordInMyFolder.getPath();
        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        Mockito.doNothing()
                .when(fileStore)
                .moveFile(Mockito.anyString(), Mockito.anyString());

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(Mockito.anyString());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        newMyFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.recordId())
                .as("문서 ID가 일치해야 합니다.")
                .isEqualTo(otherRecordInMyFolder.getRecordId());

        Assertions.assertThat(response.folder().title())
                .as("문서의 폴더 제목이 일치해야 합니다.")
                .isEqualTo(newMyFolder.getTitle());

        Assertions.assertThat(response.path())
                .as("문서의 경로가 변경되어야 합니다.")
                .isNotEqualTo(oldPath);
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (문서 X)")
    @WithAccount
    void moveRecordNotFound() throws Exception {
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        myFolder.getFolderId(),
                                        999L // 존재하지 않는 문서 ID
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (문서 소유 X)")
    @WithAccount
    void moveRecordNotMyRecord() throws Exception {
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (문서와 폴더 ID 불일치)")
    @WithAccount
    void moveRecordNotMyFolder() throws Exception {
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (타겟 폴더 X)")
    @WithAccount
    void moveRecordNotFoundFolder() throws Exception {
        RequestRecordMoveDto request = new RequestRecordMoveDto(999L); // 존재하지 않는 폴더 ID

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (폴더 소유 권한 X)")
    @WithAccount
    void moveRecordWithoutPermission() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(editor)
                        .build()
        );

        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 폴더 이동 - 실패 (타겟 폴더 접근 권한 X)")
    @WithAccount
    void moveRecordWithoutPermissionToTargetFolder() throws Exception {
        FolderEntity newMyFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(user)
                        .title("new folder")
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(newMyFolder)
                        .role(owner)
                        .build()
        );

        RequestRecordMoveDto request = new RequestRecordMoveDto(newMyFolder.getFolderId());

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/move",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 공부 시간 추가 - 성공")
    @WithAccount
    void createStudyTime() throws Exception {
        // 60분
        RequestStudyDto request = new RequestStudyDto(60L);
        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseAnalysisDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.studyTimes())
                .as("문서의 공부 시간이 60분으로 설정되어야 합니다.")
                .hasSize(1)
                .allSatisfy(studyTime -> {
                    Assertions.assertThat(studyTime.studyTime())
                            .isEqualTo(60L);
                });
    }

    @Test
    @DisplayName("문서 공부 시간 수정 - 성공")
    @WithAccount
    void updateStudyTime() throws Exception {
        // 60분
        RequestStudyDto request = new RequestStudyDto(60L);
        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 10분 추가
        RequestStudyDto updateRequest = new RequestStudyDto(10L);
        log.info("updateRequest = {}", updateRequest);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/analysis",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseAnalysisDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseAnalysisDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.studyTimes())
                .as("문서의 공부 시간이 70분으로 수정되어야 합니다.")
                .hasSize(1)
                .allSatisfy(studyTime -> {
                    Assertions.assertThat(studyTime.studyTime())
                            .isEqualTo(70L);
                });
    }

    @Test
    @DisplayName("문서 공부 시간 추가 - 실패 (문서 X)")
    @WithAccount
    void createStudyTimeNotFound() throws Exception {
        // 60분
        RequestStudyDto request = new RequestStudyDto(60L);
        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        myFolder.getFolderId(),
                                        999L // 존재하지 않는 문서 ID
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 공부 시간 추가 - 실패 (문서와 폴더 ID 불일치)")
    @WithAccount
    void createStudyTimeNotMyFolder() throws Exception {
        // 60분
        RequestStudyDto request = new RequestStudyDto(60L);
        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 공부 시간 추가 - 실패 (폴더 접근 권한 X)")
    @WithAccount
    void createStudyTimeWithoutPermission() throws Exception {
        // 60분
        RequestStudyDto request = new RequestStudyDto(60L);
        log.info("request = {}", request);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/study",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 삭제 - 성공")
    @WithAccount
    void deleteRecord() throws Exception {
        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(Mockito.anyString());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        log.info("response = {}", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("문서 삭제 - 성공 (소유자 권한)")
    @WithAccount
    void deleteRecordAsOwner() throws Exception {
        RecordEntity otherRecordInMyFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .build()
        );

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(Mockito.anyString());

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andReturn();

        log.info("response = {}", result.getResponse().getContentAsString());
    }

    @Test
    @DisplayName("문서 삭제 - 실패 (문서 X)")
    @WithAccount
    void deleteRecordNotFound() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        999L // 존재하지 않는 문서 ID
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 삭제 - 실패 (문서 소유 X)")
    @WithAccount
    void deleteRecordNotMyRecord() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 삭제 - 실패 (업로드된 문서와 폴더 ID 불일치)")
    @WithAccount
    void deleteRecordNotMyFolder() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 삭제 - 실패 (폴더 편집 권한 X)")
    @WithAccount
    void deleteRecordWithoutPermission() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .build()
        );

        // 나중에 reader 권한으로 변경된 경우
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 성공")
    @WithAccount
    void deleteRecordLinkShare() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.isShare())
                .as("문서 링크 공유가 성공적으로 이루어져야 합니다.")
                .isFalse();
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 성공 (소유자 권한)")
    @WithAccount
    void deleteRecordLinkShareAsOwner() throws Exception {
        RecordEntity otherRecordInMyFolder = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(otherUser)
                        .folder(myFolder)
                        .isShare(true) // 링크 공유가 활성화된 문서
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent())
                .andReturn();

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}",
                                        myFolder.getFolderId(),
                                        otherRecordInMyFolder.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseRecordDetailDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseRecordDetailDto.class
        );

        log.info("response = {}", response);

        Assertions.assertThat(response.isShare())
                .as("문서 링크 공유가 성공적으로 이루어져야 합니다.")
                .isFalse();
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 실패 (폴더 X)")
    @WithAccount
    void deleteRecordLinkShareNotFoundFolder() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        999L,
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound());
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 실패 (업로드된 문서와 폴더 ID 불일치)")
    @WithAccount
    void deleteRecordLinkShareNotMyFolder() throws Exception {
        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        myRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 실패 (문서 소유 X)")
    @WithAccount
    void deleteRecordLinkShareNotMyRecord() throws Exception {
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 실패 (다른 폴더에 있는 문서)")
    @WithAccount
    void deleteRecordLinkShareNotMatched() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .isShare(true) // 링크 공유가 활성화된 문서
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        myFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }

    @Test
    @DisplayName("문서 링크 공유 삭제 - 실패 (편집 권한 X)")
    @WithAccount
    void deleteRecordLinkShareWithoutPermission() throws Exception {
        RecordEntity otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .user(user)
                        .folder(otherFolder)
                        .isShare(true) // 링크 공유가 활성화된 문서
                        .build()
        );

        // 나중에 reader 권한으로 변경된 경우
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(user)
                        .folder(otherFolder)
                        .role(reader)
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.RECORD_PREFIX.getValue() + "/{recordId}/link",
                                        otherFolder.getFolderId(),
                                        otherRecord.getRecordId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden());
    }
}
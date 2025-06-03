package org.y2k2.globa.api.comment;

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
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.analysis.AnalysisFixture;
import org.y2k2.globa.fixture.comment.CommentFixture;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.highlight.HighlightFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.analysis.entity.AnalysisEntity;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.highlight.entity.HighlightEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.section.entity.SectionEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.util.JWTTestProvider;

import java.util.Map;

@Slf4j
public class CommentIntegrationTest extends IntegrationTest {
    @Autowired
    private JWT jwt;
    @Autowired
    private JWTTestProvider jwtTestProvider;
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
    private SectionFixture sectionFixture;
    @Autowired
    private AnalysisFixture analysisFixture;
    @Autowired
    private HighlightFixture highlightFixture;
    @Autowired
    private CommentFixture commentFixture;

    private UserEntity myUser;
    private UserEntity otherUser;
    private FolderRoleEntity owner;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private FolderEntity myFolder;
    private RecordEntity myRecord;
    private RecordEntity otherRecord;
    private SectionEntity mySection;
    private SectionEntity otherSection;
    private AnalysisEntity myAnalysis;
    private AnalysisEntity otherAnalysis;
    private FolderEntity otherFolder;
    private HighlightEntity myHighlight;
    private HighlightEntity otherHighlight;
    private CommentEntity myComment;

    @BeforeEach
    public void setUp() {
        myUser = userFixture.save(
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
                        .user(myUser)
                        .build()
        );
        otherFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(otherUser)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(myUser)
                        .target(myUser)
                        .folder(myFolder)
                        .role(owner)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(otherUser)
                        .target(otherUser)
                        .folder(otherFolder)
                        .role(owner)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        myRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .folder(myFolder)
                        .user(myUser)
                        .build()
        );
        otherRecord = recordFixture.save(
                RecordFixture
                        .builder()
                        .folder(otherFolder)
                        .user(otherUser)
                        .build()
        );

        mySection = sectionFixture.save(
                SectionFixture
                        .builder()
                        .record(myRecord)
                        .build()
        );
        otherSection = sectionFixture.save(
                SectionFixture
                        .builder()
                        .record(otherRecord)
                        .build()
        );

        myAnalysis = analysisFixture.save(
                AnalysisFixture
                        .builder()
                        .section(mySection)
                        .build()
        );
        otherAnalysis = analysisFixture.save(
                AnalysisFixture
                        .builder()
                        .section(otherSection)
                        .build()
        );

        myHighlight = highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(mySection)
                        .build()
        );
        otherHighlight = highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(otherSection)
                        .build()
        );


        myComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(myUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        setSecurityContext(myUser);
    }

    @Test
    @DisplayName("댓글 목록 조회 - 성공")
    void getComments_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(myUser)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        CommentEntity otherComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseCommentDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseCommentDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(2);

        Assertions
                .assertThat(response.comments())
                .isNotEmpty()
                .allSatisfy(comment -> {
                    if (comment.getUser().userId().equals(myUser.getUserId())) {
                        Assertions.assertThat(comment.getCommentId()).isEqualTo(myComment.getCommentId());
                        Assertions.assertThat(comment.getContent()).isEqualTo(myComment.getContent());
                    } else if (comment.getUser().userId().equals(otherUser.getUserId())) {
                        Assertions.assertThat(comment.getCommentId()).isEqualTo(otherComment.getCommentId());
                        Assertions.assertThat(comment.getContent()).isEqualTo(otherComment.getContent());
                    }

                    Assertions.assertThat(comment.getHasReply()).isEqualTo(false);
                    Assertions.assertThat(comment.getDeleted()).isEqualTo(false);
                });
    }

    @Test
    @DisplayName("댓글 목록 조회 - 실패 (권한 X)")
    @WithAccount
    void getComments_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 목록 조회 - 실패 (하이라이트 X)")
    @WithAccount
    void getComments_Fail_HighlightNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = 999L; // 존재하지 않는 하이라이트 ID

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 성공")
    @WithAccount
    void getReplies_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        CommentEntity replyMyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(myUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(myUser)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );
        CommentEntity replyOtherComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        ResponseReplyDto response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                ResponseReplyDto.class
        );

        log.info("response = {}", response);

        Assertions
                .assertThat(response.total())
                .isEqualTo(2);

        Assertions
                .assertThat(response.comments())
                .isNotEmpty()
                .allSatisfy(comment -> {
                    if (comment.getUser().userId().equals(myUser.getUserId())) {
                        Assertions.assertThat(comment.getCommentId()).isEqualTo(replyMyComment.getCommentId());
                        Assertions.assertThat(comment.getContent()).isEqualTo(replyMyComment.getContent());
                    } else if (comment.getUser().userId().equals(otherUser.getUserId())) {
                        Assertions.assertThat(comment.getCommentId()).isEqualTo(replyOtherComment.getCommentId());
                        Assertions.assertThat(comment.getContent()).isEqualTo(replyOtherComment.getContent());
                    }

                    Assertions.assertThat(comment.getDeleted()).isEqualTo(false);
                });
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 실패 (권한 X)")
    @WithAccount
    void getReplies_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_ACCESS_FOLDER.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 실패 (하이라이트 X)")
    @WithAccount
    void getReplies_Fail_HighlightNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = 999L, // 존재하지 않는 하이라이트 ID
                parentId = myComment.getCommentId();

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode()));
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 실패 (부모 댓글 X)")
    @WithAccount
    void getReplies_Fail_ParentCommentNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = 999L; // 존재하지 않는 부모 댓글 ID

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                                .param("page", "1")
                                .param("count", "10")
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_PARENT_COMMENT.getErrorCode()));
    }

    @Test
    @DisplayName("첫 댓글 추가 - 성공 (Owner)")
    @WithAccount
    void addComment_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId();

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"));
    }

    @Test
    @DisplayName("첫 댓글 추가 - 성공 (Editor)")
    @WithAccount
    void addComment_Success_Editor() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId();

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(myUser)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(editor)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().exists("Location"));
    }

    @Test
    @DisplayName("첫 댓글 추가 - 실패 (Reader)")
    @WithAccount
    void addComment_Fail_Reader() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId();

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        folderShareFixture.save(
                FolderShareFixture
                        .builder()
                        .owner(myUser)
                        .target(otherUser)
                        .folder(myFolder)
                        .role(reader)
                        .status(InvitationStatus.ACCEPT)
                        .build()
        );

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_WRITEABLE.getErrorCode()));
    }

    @Test
    @DisplayName("첫 댓글 추가 - 실패 (하이라이트 중복)")
    @WithAccount
    void addComment_Fail_HighlightDuplicated() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId();

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(mySection)
                        .startIndex(request.startIdx())
                        .endIndex(request.endIdx())
                        .build()
        );

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isConflict())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.HIGHLIGHT_DUPLICATED.getErrorCode()));
    }

    @Test
    @DisplayName("첫 댓글 추가 - 실패 (섹션 X)")
    @WithAccount
    void addComment_Fail_SectionNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = 999L; // 존재하지 않는 섹션 ID

        RequestFirstCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_SECTION.getErrorCode()));
    }
}

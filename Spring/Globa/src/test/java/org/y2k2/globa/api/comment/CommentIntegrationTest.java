package org.y2k2.globa.api.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.y2k2.globa.annotation.WithAccount;
import org.y2k2.globa.api.IntegrationTest;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;
import org.y2k2.globa.fixture.comment.CommentFixture;
import org.y2k2.globa.fixture.folder.FolderFixture;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.foldershare.FolderShareFixture;
import org.y2k2.globa.fixture.highlight.HighlightFixture;
import org.y2k2.globa.fixture.record.RecordFixture;
import org.y2k2.globa.fixture.section.SectionFixture;
import org.y2k2.globa.fixture.user.UserFixture;
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
    private JdbcTemplate jdbcTemplate;

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
    private HighlightFixture highlightFixture;
    @Autowired
    private CommentFixture commentFixture;

    private UserEntity myUser;
    private UserEntity otherUser;
    private FolderRoleEntity editor;
    private FolderRoleEntity reader;
    private FolderEntity myFolder;
    private RecordEntity myRecord;
    private SectionEntity mySection;
    private HighlightEntity myHighlight;
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
        FolderRoleEntity owner = folderRoleFixture.getEntity(FolderRole.OWNER);
        editor = folderRoleFixture.getEntity(FolderRole.EDITOR);
        reader = folderRoleFixture.getEntity(FolderRole.READER);

        myFolder = folderFixture.save(
                FolderFixture
                        .builder()
                        .user(myUser)
                        .build()
        );
        FolderEntity otherFolder = folderFixture.save(
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
        recordFixture.save(
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

        myHighlight = highlightFixture.save(
                HighlightFixture
                        .builder()
                        .section(mySection)
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

    @AfterEach
    public void tearDown() {
        jdbcTemplate.execute("DELETE FROM comment");
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
    void addFirstComment_Success() throws Exception {
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
    void addFirstComment_Success_Editor() throws Exception {
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
    void addFirstComment_Fail_Reader() throws Exception {
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
    void addFirstComment_Fail_HighlightDuplicated() throws Exception {
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
    void addFirstComment_Fail_SectionNotFound() throws Exception {
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

    @Test
    @DisplayName("부모 댓글 추가 - 성공 (Owner)")
    @WithAccount
    void addParentComment_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
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
    @DisplayName("부모 댓글 추가 - 성공 (Editor)")
    @WithAccount
    void addParentComment_Success_Editor() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

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
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
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
    @DisplayName("부모 댓글 추가 - 실패 (Reader)")
    @WithAccount
    void addParentComment_Fail_Reader() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

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
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
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
    @DisplayName("부모 댓글 추가 - 실패 (권한 X)")
    @WithAccount
    void addParentComment_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        setSecurityContext(otherUser);

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
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
    @DisplayName("대댓글 추가 - 성공 (Owner)")
    @WithAccount
    void addReply_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
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
    @DisplayName("대댓글 추가 - 성공 (Editor)")
    @WithAccount
    void addReply_Success_Editor() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

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
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
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
    @DisplayName("대댓글 추가 - 실패 (Reader)")
    @WithAccount
    void addReply_Fail_Reader() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

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
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
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
    @DisplayName("대댓글 추가 - 실패 (권한 X)")
    @WithAccount
    void addReply_Fail_NoPermission() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = myComment.getCommentId();

        setSecurityContext(otherUser);

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
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
    @DisplayName("대댓글 추가 - 실패 (부모 댓글 X)")
    @WithAccount
    void addReply_Fail_ParentCommentNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                parentId = 999L; // 존재하지 않는 부모 댓글 ID

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_PARENT_COMMENT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 수정 - 성공 (부모, Owner)")
    @WithAccount
    void updateParentComment_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = myComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 - 성공 (부모, Editor)")
    @WithAccount
    void updateParentComment_Success_Editor() throws Exception {
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

        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = otherCommentInMyFolder.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 - 성공 (대댓글, Owner)")
    @WithAccount
    void updateReply_Success() throws Exception {
        CommentEntity replyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(myUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = replyComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 - 성공 (대댓글, Editor)")
    @WithAccount
    void updateReply_Success_Editor() throws Exception {
        CommentEntity replyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
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

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = replyComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (부모, Reader)")
    @WithAccount
    void updateParentComment_Fail_Reader() throws Exception {
        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = otherCommentInMyFolder.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        // Reader 권한으로 변경되었음.
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
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
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
    @DisplayName("댓글 수정 - 실패 (대댓글, Reader)")
    @WithAccount
    void updateReply_Fail_Reader() throws Exception {
        CommentEntity replyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = replyComment.getCommentId();

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        // Reader 권한으로 변경되었음.
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
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
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
    @DisplayName("댓글 수정 - 실패 (댓글 X)")
    @WithAccount
    void updateComment_Fail_CommentNotFound() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = 999L; // 존재하지 않는 댓글 ID

        RequestCommentDto request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_COMMENT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (부모, 마지막, Owner)")
    @WithAccount
    void deleteParentComment_Success() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = myComment.getCommentId();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 댓글이 삭제되었는지 확인
        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                // 삭제한 댓글이 마지막이기 때문에 Highlight가 존재하지 않음
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (부모, 대댓글 존재, Owner)")
    @WithAccount
    void deleteParentComment_Success_WithReplies() throws Exception {
        // 대댓글 생성
        commentFixture.save(
                CommentFixture
                        .builder()
                        .user(myUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = myComment.getCommentId();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 댓글이 삭제되었는지 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)                                .characterEncoding("UTF-8")
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
                .isEqualTo(1L);

        // 대댓글이 존재하기 때문에 Soft Delete
        Assertions
                .assertThat(response.comments())
                .hasSize(1)
                .allSatisfy(comment -> {
                    Assertions.assertThat(comment.getCommentId()).isEqualTo(myComment.getCommentId());
                    Assertions.assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다.");
                    Assertions.assertThat(comment.getDeleted()).isTrue();
                    Assertions.assertThat(comment.getHasReply()).isTrue();
                });
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (부모, 마지막, Editor)")
    @WithAccount
    void deleteParentComment_Success_Editor() throws Exception {
        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
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

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        // 부모댓글 삭제
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, myComment.getCommentId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        setSecurityContext(otherUser);

        // 대댓글 삭제
        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, otherCommentInMyFolder.getCommentId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 댓글이 삭제되었는지 확인
        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId, otherCommentInMyFolder.getCommentId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNotFound())
                // 삭제한 댓글이 마지막이기 때문에 Highlight가 존재하지 않음
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_FOUND_HIGHLIGHT.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (대댓글, 대댓글 존재, Editor)")
    @WithAccount
    void deleteReply_Success_Editor() throws Exception {
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

        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        CommentEntity replyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .parent(otherCommentInMyFolder)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = otherCommentInMyFolder.getCommentId();

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 댓글이 삭제되었는지 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                        folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON_VALUE)                                .characterEncoding("UTF-8")
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
                .isEqualTo(2L);

        // 대댓글이 존재하기 때문에 Soft Delete
        Assertions
                .assertThat(response.comments())
                .hasSize(2)
                .allSatisfy(comment -> {
                    if (comment.getCommentId().equals(otherCommentInMyFolder.getCommentId())) {
                        Assertions.assertThat(comment.getContent()).isEqualTo("삭제된 댓글입니다.");
                        Assertions.assertThat(comment.getDeleted()).isTrue();
                        Assertions.assertThat(comment.getHasReply()).isTrue();
                    }
                });
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (Reader)")
    @WithAccount
    void deleteComment_Fail_Reader() throws Exception {
        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = otherCommentInMyFolder.getCommentId();

        // Reader 권한으로 변경되었음.
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
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_WRITEABLE.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (권한 X)")
    @WithAccount
    void deleteComment_Fail_NoPermission() throws Exception {
        CommentEntity otherCommentInMyFolder = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(otherUser)
                        .highlight(myHighlight)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = otherCommentInMyFolder.getCommentId();

        setSecurityContext(otherUser);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.NOT_DESERVE_WRITEABLE.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 실패 (댓글 작성자 X)")
    @WithAccount
    void deleteComment_Fail_NoWriter() throws Exception {
        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId(),
                commentId = myComment.getCommentId();

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
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isForbidden())
                .andExpect(MockMvcResultMatchers.jsonPath("$.errorCode").value(ErrorCode.MISMATCH_COMMENT_OWNER.getErrorCode()));
    }

    @Test
    @DisplayName("댓글 삭제 - 성공 (대댓글)")
    @WithAccount
    void deleteReply_Success() throws Exception {
        CommentEntity replyComment = commentFixture.save(
                CommentFixture
                        .builder()
                        .user(myUser)
                        .highlight(myHighlight)
                        .parent(myComment)
                        .deleted(false)
                        .build()
        );

        Long folderId = myFolder.getFolderId(),
                recordId = myRecord.getRecordId(),
                sectionId = mySection.getSectionId(),
                highlightId = myHighlight.getHighlightId();

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                        folderId, recordId, sectionId, highlightId, replyComment.getCommentId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andDo(MockMvcResultHandlers.print())
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        // 댓글이 삭제되었는지 확인
        MvcResult result = mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                        Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                        folderId, recordId, sectionId, highlightId, myComment.getCommentId()
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .accept(MediaType.APPLICATION_JSON)
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
                .isEqualTo(1L);

        // 대댓글이 삭제되었는지 확인
        Assertions
                .assertThat(response.comments())
                .hasSize(1)
                .allSatisfy(comment -> {
                    Assertions.assertThat(comment.getContent()).isEqualTo("삭제된 답글입니다.");
                    Assertions.assertThat(comment.getDeleted()).isTrue();
                });
    }
}

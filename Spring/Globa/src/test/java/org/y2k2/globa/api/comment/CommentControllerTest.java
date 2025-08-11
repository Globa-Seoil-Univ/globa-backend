package org.y2k2.globa.api.comment;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
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
import org.y2k2.globa.api.CommentController;
import org.y2k2.globa.api.ControllerConfig;
import org.y2k2.globa.application.comment.dto.request.RequestCommentDto;
import org.y2k2.globa.application.comment.dto.request.RequestCommentWithIdsDto;
import org.y2k2.globa.application.comment.dto.request.RequestFirstCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseCommentDto;
import org.y2k2.globa.application.comment.dto.response.ResponseReplyDto;
import org.y2k2.globa.application.comment.service.*;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.constant.Constant;

import java.nio.charset.StandardCharsets;

@Slf4j
@ActiveProfiles("test")
@Import(ControllerConfig.class)
@WebMvcTest(controllers = CommentController.class)
@AutoConfigureMockMvc(addFilters = false)
public class CommentControllerTest {
    @Autowired
    private JWT jwt;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private GetCommentsService getCommentsService;
    @MockBean
    private GetRepliesService getRepliesService;
    @MockBean
    private CreateFirstCommentService createFirstCommentService;
    @MockBean
    private CreateParentCommentService createParentCommentService;
    @MockBean
    private CreateReplyService createReplyService;
    @MockBean
    private UpdateCommentService updateCommentService;
    @MockBean
    private DeleteCommentService deleteCommentService;

    @Test
    @DisplayName("댓글 목록 조회 - 성공")
    @WithAccount
    void getComments() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L;

        int page = 1,
            count = 10;

        ResponseCommentDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseCommentDto.class);

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .build();

        Mockito
                .when(getCommentsService.get(request, page, count))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getCommentsService, Mockito.times(1))
                .get(request, page, count);
    }

    @Test
    @DisplayName("대댓글 목록 조회 - 성공")
    @WithAccount
    void getReplies() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                parentId = 1L;

        int page = 1,
            count = 10;

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .parentId(parentId)
                .build();

        ResponseReplyDto response = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeOne(ResponseReplyDto.class);

        Mockito
                .when(getRepliesService.get(request, page, count))
                .thenReturn(response);

        mockMvc.perform(
                        MockMvcRequestBuilders.get(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .param("page", String.valueOf(page))
                                .param("count", String.valueOf(count))
                                .accept(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

        log.info("response = {}", response);

        Mockito.verify(getRepliesService, Mockito.times(1))
                .get(request, page, count);
    }

    @Test
    @DisplayName("첫 댓글 작성 - 성공")
    @WithAccount
    void createFirstComment() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestFirstCommentDto.class);

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .build();

        String location = String.format(
                "/folder/%d/record/%d/section/%d/highlight/%d/comment",
                folderId, recordId, sectionId, highlightId
        );

        Mockito
                .when(createFirstCommentService.create(request, dto))
                .thenReturn(highlightId);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", location));

        log.info("created highlight id = {}", highlightId);
    }

    @Test
    @DisplayName("첫 댓글 작성 - 실패 (시작 Index, 끝 Index가 같을 때)")
    @WithAccount
    void createFirstCommentFail() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestFirstCommentDto.class)
                .set("startIdx", 5L)
                .set("endIdx", 5L)
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("첫 댓글 작성 - 실패 (시작 Index가 끝 Index보다 클 때)")
    @WithAccount
    void createFirstCommentFail2() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestFirstCommentDto.class)
                .set("startIdx", 5L)
                .set("endIdx", 3L)
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("첫 댓글 작성 - 실패 (시작 Index가 음수일 때)")
    @WithAccount
    void createFirstCommentFail3() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestFirstCommentDto.class)
                .set("startIdx", -1L)
                .set("endIdx", 3L)
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("첫 댓글 작성 - 실패 (끝 Index가 음수일 때)")
    @WithAccount
    void createFirstCommentFail4() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestFirstCommentDto.class)
                .set("startIdx", 1L)
                .set("endIdx", -1L)
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("첫 댓글 작성 - 실패 (내용이 비어있을 때)")
    @WithAccount
    void createFirstCommentFail5() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L;

        RequestFirstCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestFirstCommentDto.class)
                .set("content", "")
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}",
                                folderId, recordId, sectionId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("댓글 작성 - 성공")
    @WithAccount
    void createParentComment() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .build();

        String location = String.format(
                "/folder/%d/record/%d/section/%d/highlight/%d/comment",
                folderId, recordId, sectionId, highlightId
        );

        Mockito
                .doNothing()
                .when(createParentCommentService)
                .create(request, dto);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", location));

        log.info("created highlight id = {}", highlightId);
    }

    @Test
    @DisplayName("댓글 작성 - 실패 (내용이 비어있을 때)")
    @WithAccount
    void createParentCommentFail() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestCommentDto.class)
                .set("content", "")
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment",
                                folderId, recordId, sectionId, highlightId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("대댓글 작성 - 성공")
    @WithAccount
    void createReply() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                parentId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .parentId(parentId)
                .build();

        String location = String.format(
                "/folder/%d/record/%d/section/%d/highlight/%d/comment/%d",
                folderId, recordId, sectionId, highlightId, parentId
        );

        Mockito
                .doNothing()
                .when(createReplyService)
                .create(request, dto);

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.header().string("Location", location));

        log.info("created highlight id = {}", highlightId);
    }

    @Test
    @DisplayName("대댓글 작성 - 실패 (내용이 비어있을 때)")
    @WithAccount
    void createReplyFail() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                parentId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestCommentDto.class)
                .set("content", "")
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.post(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}",
                                folderId, recordId, sectionId, highlightId, parentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("댓글 수정 - 성공")
    @WithAccount
    void updateComment() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                commentId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .plugin(new JakartaValidationPlugin())
                .build()
                .giveMeOne(RequestCommentDto.class);

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .build();

        Mockito
                .doNothing()
                .when(updateCommentService)
                .update(request, commentId, dto);

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        log.info("updated comment id = {}", commentId);
    }

    @Test
    @DisplayName("댓글 수정 - 실패 (내용이 비어있을 때)")
    @WithAccount
    void updateCommentFail() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                commentId = 1L;

        RequestCommentDto dto = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(RequestCommentDto.class)
                .set("content", "")
                .sample();

        mockMvc.perform(
                        MockMvcRequestBuilders.patch(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(result -> {
                    log.error("Error response: {}",
                            result.getResponse().getContentAsString(StandardCharsets.UTF_8));
                });
    }

    @Test
    @DisplayName("댓글 삭제 - 성공")
    @WithAccount
    void deleteComment() throws Exception {
        Long folderId = 1L,
                recordId = 1L,
                sectionId = 1L,
                highlightId = 1L,
                commentId = 1L;

        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .userId(1L)
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .build();

        Mockito
                .doNothing()
                .when(deleteCommentService)
                .delete(request, commentId);

        mockMvc.perform(
                        MockMvcRequestBuilders.delete(
                                Constant.COMMENT_PREFIX.getValue() + "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}",
                                folderId, recordId, sectionId, highlightId, commentId
                                )
                                .header(Constant.JWT_HEADER.getValue(), jwt.getGrantType() + jwt.getAccessToken())
                )
                .andExpect(MockMvcResultMatchers.status().isNoContent());

        log.info("deleted comment id = {}", commentId);
    }
}

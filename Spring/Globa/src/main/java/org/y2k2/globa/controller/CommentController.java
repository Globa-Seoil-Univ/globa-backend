package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.dto.request.comment.RequestCommentDto;
import org.y2k2.globa.dto.request.comment.RequestCommentWithIdsDto;
import org.y2k2.globa.dto.request.comment.RequestFirstCommentDto;
import org.y2k2.globa.dto.response.comment.ResponseCommentDto;
import org.y2k2.globa.dto.response.comment.ResponseReplyDto;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.CommentService;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.net.URI;

@RestController
@RequestMapping("/folder/{folderId}/record/{recordId}")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관련 API입니다.")
public class CommentController {
    private final CommentService commentService;

    @Operation(
            summary = "댓글 목록 조회",
            description = """
                    댓글 목록을 조회합니다. <br />
                    단, 최상위 댓글만 조회합니다. (대댓글 X)
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseCommentDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> getComments(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .user(details.getUser())
                .build();
        ResponseCommentDto commentDto = commentService.getComments(request, page, count);
        return ResponseEntity.ok().body(commentDto);
    }

    @Operation(
            summary = "대댓글 목록 조회",
            description = "대댓글 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "대댓글 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseReplyDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> getReply(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @PathVariable("parentId") Long parentId,
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .parentId(parentId)
                .user(details.getUser())
                .build();
        ResponseReplyDto replyDto = commentService.getReply(request, page, count);
        return ResponseEntity.ok().body(replyDto);
    }

    @Operation(
            summary = "첫 댓글 추가",
            description = """
                    첫 댓글을 추가합니다. <br />
                    하이라이트 내에서 최초 댓글을 작성할 때 사용합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "댓글 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                    })),
                    @ApiResponse(responseCode = "409", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.HIGHLIGHT_DUPLICATED, ref = SwaggerErrorCode.HIGHLIGHT_DUPLICATED_VALUE)
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/section/{sectionId}")
    public ResponseEntity<?> addFirstComment(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @Valid @RequestBody RequestFirstCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .user(details.getUser())
                .build();
        Long highlightId = commentService.addFirstComment(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId  + "/comment")
        ).build();
    }

    @Operation(
            summary = "댓글 추가",
            description = "댓글을 추가 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "댓글 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @Valid @RequestBody final RequestCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .user(details.getUser())
                .build();

        commentService.addComment(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId + "/comment")
        ).build();
    }

    @Operation(
            summary = "대댓글 추가",
            description = "대댓글을 추가 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "대댓글 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> addReply(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @PathVariable("parentId") Long parentId,
            @Valid @RequestBody final RequestCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .parentId(parentId)
                .user(details.getUser())
                .build();

        commentService.addReply(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId + "/comment/" + parentId)
        ).build();
    }

    @Operation(
            summary = "댓글 수정",
            description = """
                    댓글을 수정합니다.
                    댓글 수정은 최상위, 대댓글 모두 가능합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "댓글 수정 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_COMMENT_OWNER, ref = SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> updateComment(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @PathVariable("commentId") Long commentId,
            @Valid @RequestBody final RequestCommentDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .user(details.getUser())
                .build();

        commentService.updateComment(request, commentId, dto);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "댓글 삭제",
            description = """
                    댓글을 삭제합니다.
                    댓글 삭제는 최상위, 대댓글 모두 가능합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "댓글 삭제 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_COMMENT_OWNER, ref = SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @PathVariable("highlightId") Long highlightId,
            @PathVariable("commentId") Long commentId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        RequestCommentWithIdsDto request = RequestCommentWithIdsDto.builder()
                .folderId(folderId)
                .recordId(recordId)
                .sectionId(sectionId)
                .highlightId(highlightId)
                .user(details.getUser())
                .build();

        commentService.deleteComment(request, commentId);
        return ResponseEntity.noContent().build();
    }

}

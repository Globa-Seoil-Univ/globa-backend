package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.CommentService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/folder/{folderId}/record/{recordId}")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Comment", description = "댓글 관련 API입니다.")
public class CommentController {
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "댓글 목록 조회",
            description = """
                    댓글 목록을 조회합니다. <br />
                    단, 최상위 댓글만 조회합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "댓글 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseCommentDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> getComments(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count
    ) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> getReply(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("parentId") long parentId,
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId, parentId);
        ResponseReplyDto replyDto = commentService.getReply(request, page, count);
        return ResponseEntity.ok().body(replyDto);
    }

    @Operation(
            summary = "첫 댓글 추가",
            description = """
                    첫 댓글을 추가합니다.
                    하이라이트 내에서 최초 댓글을 작성할 때 사용합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "댓글 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
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
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @Valid @RequestBody final RequestFirstCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId);
        long highlightId = commentService.addFirstComment(request, dto);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> addComment(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_PARENT_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> addReply(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("parentId") long parentId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId, parentId);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_COMMENT_OWNER, ref = SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> updateComment(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("commentId") long commentId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto requestCommentWithIdsDto = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        commentService.updateComment(requestCommentWithIdsDto, commentId, dto);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT, ref = SwaggerErrorCode.NOT_DESERVE_POST_COMMENT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_COMMENT_OWNER, ref = SwaggerErrorCode.MISMATCH_COMMENT_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_SECTION, ref = SwaggerErrorCode.NOT_FOUND_SECTION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT, ref = SwaggerErrorCode.NOT_FOUND_HIGHLIGHT_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_COMMENT, ref = SwaggerErrorCode.NOT_FOUND_COMMENT_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("commentId") long commentId
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        RequestCommentWithIdsDto requestCommentWithIdsDto = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        commentService.deleteComment(requestCommentWithIdsDto, commentId);
        return ResponseEntity.noContent().build();
    }

}

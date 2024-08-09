package org.y2k2.globa.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.CommentService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/folder/{folderId}/record/{recordId}")
@ResponseBody
@RequiredArgsConstructor
public class CommentController {
    private final CommentService commentService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> getComments(
            @RequestHeader(value = "Authorization") String accessToken,
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
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        ResponseCommentDto commentDto = commentService.getComments(request, page, count);
        return ResponseEntity.ok().body(commentDto);
    }

    @GetMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> getReply(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("parentId") long parentId,
            @RequestParam(required = false, defaultValue = "1", value = "page") int page,
            @RequestParam(required = false, defaultValue = "10", value = "count") int count
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId, parentId);
        ResponseReplyDto replyDto = commentService.getReply(request, page, count);
        return ResponseEntity.ok().body(replyDto);
    }

    @PostMapping(value = "/section/{sectionId}")
    public ResponseEntity<?> addFirstComment(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @Valid @RequestBody final RequestFirstCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId);
        long highlightId = commentService.addFirstComment(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId  + "/comment")
        ).build();
    }

    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> addComment(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        commentService.addComment(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId + "/comment")
        ).build();
    }

    @PostMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> addReply(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("parentId") long parentId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId, parentId);
        commentService.addReply(request, dto);
        return ResponseEntity.created(
                URI.create("/folder/" + folderId + "/record/" + recordId + "/section/" + sectionId + "/highlight/" + highlightId + "/comment/" + parentId)
        ).build();
    }

    @PatchMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> updateComment(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("commentId") long commentId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto requestCommentWithIdsDto = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        commentService.updateComment(requestCommentWithIdsDto, commentId, dto);
        return ResponseEntity.noContent().build();
    }
    @DeleteMapping(value = "/section/{sectionId}/highlight/{highlightId}/comment/{commentId}")
    public ResponseEntity<?> deleteComment(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("commentId") long commentId
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        RequestCommentWithIdsDto requestCommentWithIdsDto = new RequestCommentWithIdsDto(userId, folderId, recordId, sectionId, highlightId);
        commentService.deleteComment(requestCommentWithIdsDto, commentId);
        return ResponseEntity.noContent().build();
    }

}

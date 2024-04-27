package org.y2k2.globa.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.RequestCommentDto;
import org.y2k2.globa.dto.RequestCommentWithIdsDto;
import org.y2k2.globa.dto.RequestFirstCommentDto;
import org.y2k2.globa.service.CommentService;

import java.net.URI;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class CommentController {
    private final String PRE_FIX = "/folder/{folderId}/record/{recordId}";
    private final CommentService commentService;

    @PostMapping(value = PRE_FIX + "/section/{sectionId}")
    public ResponseEntity<?> addFirstComment(
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @Valid @RequestBody final RequestFirstCommentDto dto
    ) {
        // token 체크

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(1L, folderId, recordId, sectionId);
        long highlightId = commentService.addFirstComment(request, dto);
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/record/" + recordId + "/highlight/" + highlightId)).build();
    }

    @PostMapping(value = PRE_FIX + "/section/{sectionId}/highlight/{highlightId}/comment")
    public ResponseEntity<?> addComment(
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @Valid @RequestBody final RequestCommentDto dto
            ) {
        // token 체크

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(1L, folderId, recordId, sectionId, highlightId);
        commentService.addComment(request, dto);
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/record/" + recordId + "/highlight/" + highlightId)).build();
    }

    @PostMapping(value = PRE_FIX + "/section/{sectionId}/highlight/{highlightId}/comment/{parentId}")
    public ResponseEntity<?> addReply(
            @PathVariable("folderId") long folderId,
            @PathVariable("recordId") long recordId,
            @PathVariable("sectionId") long sectionId,
            @PathVariable("highlightId") long highlightId,
            @PathVariable("parentId") long parentId,
            @Valid @RequestBody final RequestCommentDto dto
    ) {
        // token 체크

        RequestCommentWithIdsDto request = new RequestCommentWithIdsDto(1L, folderId, recordId, sectionId, highlightId, parentId);
        commentService.addReply(request, dto);
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/record/" + recordId + "/highlight/" + highlightId)).build();
    }
}

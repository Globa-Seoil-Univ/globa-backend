package org.y2k2.globa.controller;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
            @PathVariable("folderId") Long folderId,
            @PathVariable("recordId") Long recordId,
            @PathVariable("sectionId") Long sectionId,
            @Valid @RequestBody final RequestFirstCommentDto dto
    ) {
        // token 체크

        long highlightId = commentService.addFirstComment(1L, folderId, recordId, sectionId, dto);
        return ResponseEntity.created(URI.create("/folder/" + folderId + "/record/" + recordId + "/highlight/" + highlightId)).build();
    }
}

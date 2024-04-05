package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.NoticeAddRequestDto;
import org.y2k2.globa.dto.NoticeIntroResponseDto;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.mapper.NoticeMapper;
import org.y2k2.globa.service.NoticeService;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class NoticeController {
    private final String PRE_FIX = "/notice";
    private final NoticeService noticeService;

    @GetMapping(PRE_FIX + "/intro")
    public ResponseEntity<?> getIntroNotices() {
        // token 체크
        return ResponseEntity.ok().body(noticeService.getIntroNotices());
    }

    @GetMapping(PRE_FIX + "/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable("noticeId") Long noticeId) {
        // token 체크
        if (noticeId == null) throw new BadRequestException("You must request notice id");

        return ResponseEntity.ok().body(noticeService.getNoticeDetail(noticeId));
    }

    @PostMapping(value = PRE_FIX, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addNotice(@Valid @ModelAttribute final NoticeAddRequestDto dto) {
        // token 체크

        Long noticeId = noticeService.addNotice(1L, dto);
        return ResponseEntity.created(URI.create(PRE_FIX + "/" + noticeId)).build();
    }
}

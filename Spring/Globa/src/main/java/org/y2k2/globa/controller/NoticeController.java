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
        List<NoticeEntity> noticeEntities = noticeService.getIntroNotices();
        List<NoticeIntroResponseDto> introDtoList = new ArrayList<>();

        for (NoticeEntity noticeEntity : noticeEntities) {
            NoticeIntroResponseDto introDto = NoticeMapper.INSTANCE.toIntroResponseDto(noticeEntity);
            introDtoList.add(introDto);
        }

        return ResponseEntity.ok().body(introDtoList);
    }

    @GetMapping(PRE_FIX + "/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable("noticeId") Long noticeId) {
        if (noticeId == null) throw new BadRequestException("You must request notice id");

        NoticeEntity entity = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok().body(NoticeMapper.INSTANCE.toDetailResponseDto(entity));
    }

    @PostMapping(value = PRE_FIX, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addNotice(@Valid @ModelAttribute final NoticeAddRequestDto dto) {
        Long noticeId = noticeService.addNotice(1L, dto);
        return ResponseEntity.created(URI.create("/notice/" + noticeId)).build();
    }
}

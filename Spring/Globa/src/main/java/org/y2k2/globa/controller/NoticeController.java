package org.y2k2.globa.controller;

import jdk.jfr.ContentType;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.NoticeIntroResponseDto;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.mapper.NoticeMapper;
import org.y2k2.globa.service.NoticeService;

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
        NoticeEntity entity = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok().body(NoticeMapper.INSTANCE.toDetailResponseDto(entity));
    }
}

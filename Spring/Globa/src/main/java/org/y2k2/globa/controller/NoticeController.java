package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.dto.NoticeDto;
import org.y2k2.globa.dto.NoticeIntroDto;
import org.y2k2.globa.entity.NoticeEntity;
import org.y2k2.globa.service.NoticeService;

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
        return ResponseEntity.ok().body(NoticeIntroDto.fromArray(noticeEntities));
    }

    @GetMapping(PRE_FIX + "/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@PathVariable("noticeId") Long noticeId) {
        NoticeEntity entity = noticeService.getNoticeDetail(noticeId);
        return ResponseEntity.ok().body(NoticeDto.from(entity));
    }
}

package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.NoticeAddRequestDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.NoticeService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/notice")
@ResponseBody
@RequiredArgsConstructor
public class NoticeController {
    private final NoticeService noticeService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping("/intro")
    public ResponseEntity<?> getIntroNotices(@RequestHeader(value = "Authorization") String accessToken) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);
        return ResponseEntity.ok().body(noticeService.getIntroNotices());
    }

    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@RequestHeader(value = "Authorization") String accessToken, @PathVariable("noticeId") Long noticeId) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (noticeId == null) throw new BadRequestException("You must request notice id");

        return ResponseEntity.ok().body(noticeService.getNoticeDetail(noticeId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addNotice(@RequestHeader(value = "Authorization") String accessToken, @Valid @ModelAttribute final NoticeAddRequestDto dto) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        Long noticeId = noticeService.addNotice(userId, dto);
        return ResponseEntity.created(URI.create("/" + noticeId)).build();
    }
}

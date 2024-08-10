package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.NotificationService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/notification")
@ResponseBody
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<?> getNotifications(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        return ResponseEntity.ok().body(notificationService.getNotifications(userId, count, page));
    }
}

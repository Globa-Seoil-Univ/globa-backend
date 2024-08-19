package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.NotificationService;
import org.y2k2.globa.util.JwtTokenProvider;
import org.y2k2.globa.util.ValidValues;

import java.util.List;

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
        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        return ResponseEntity.ok().body(notificationService.getNotifications(userId, count, page));
    }

    @GetMapping("/unread/check")
    public ResponseEntity<?> getHasUnreadNotifications(
            @RequestHeader(value = "Authorization") String accessToken
        ) {
        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);


        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        return ResponseEntity.ok().body(notificationService.getHasUnreadNotification(userId));
    }

    @GetMapping("/unread")
    public ResponseEntity<?> getUnreadNotifications(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam(value="type", defaultValue = "a") String type
    ) {
        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        if ( !ValidValues.validNotificationTypes.contains(type))
            throw new CustomException(ErrorCode.NOFI_TYPE_BAD_REQUEST);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        return ResponseEntity.ok().body(notificationService.getUnreadNotification(userId, type));
    }

    @GetMapping("/unread/count")
    public ResponseEntity<?> getCountUnreadNotifications(
            @RequestHeader(value = "Authorization") String accessToken
    ) {
        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);


        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        return ResponseEntity.ok().body(notificationService.getCountUnreadNotification(userId));
    }

    @PostMapping("/{notification_id}")
    public ResponseEntity<?> postNotificationRead(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value="notification_id") Long notificationId
    ) {
        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        if (notificationId == null || notificationId < 0)
            throw new CustomException(ErrorCode.REQUIRED_NOTIFICATION_ID);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        notificationService.postNotificationRead(userId,notificationId);

        return ResponseEntity.ok().body("");
    }

    @DeleteMapping("/{notification_id}")
    public ResponseEntity<?> deleteNotification(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable(value="notification_id") Long notificationId
    ) {
        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        if (notificationId == null || notificationId < 0)
            throw new CustomException(ErrorCode.REQUIRED_NOTIFICATION_ID);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        notificationService.deleteNotification(userId,notificationId);

        return ResponseEntity.ok().body("");
    }



}

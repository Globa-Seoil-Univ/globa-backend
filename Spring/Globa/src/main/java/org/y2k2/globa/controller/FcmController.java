package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.FcmService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/fcm")
@ResponseBody
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send")
    public ResponseEntity<?> pushMessage(@RequestHeader(value = "Authorization") String accessToken, @RequestBody RequestFcmTopicDto requestFcmTopicDto) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);

        fcmService.sendTopicNotification(requestFcmTopicDto);
        return ResponseEntity.noContent().build();
    }
}

package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.service.FcmService;

@RestController
@RequestMapping("/fcm")
@ResponseBody
@RequiredArgsConstructor
public class FcmController {
    private final FcmService fcmService;

    @PostMapping("/send")
    public ResponseEntity<?> pushMessage(@RequestBody RequestFcmTopicDto requestFcmTopicDto) {
        fcmService.sendTopicNotification(requestFcmTopicDto);
        return ResponseEntity.noContent().build();
    }
}

package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.Parameters;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.UnAuthorizedException;
import org.y2k2.globa.service.FcmService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/fcm")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Fcm", description = "Firebase Cloud Messaging을 사용하여 특정 토픽에게 알림을 보내는 API입니다.")
public class FcmController {
    private final FcmService fcmService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send")
    @Operation(summary = "특정 토픽 알림 전송", description = "특정 토픽에게 알림을 보냅니다. 공지사항, 이벤트 알림에서 사용할 수 있습니다.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "알림 전송 성공", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "400", description = "필요 정보가 부족 (주제, 제목, 내용)", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "401", description = "액세스 토큰 인증 실패", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "40110", description = "유효하지 않은 액세스 토큰", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "40120", description = "만료된 액세스 토큰", content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "500", description = "서버에서 발생한 에러", content = @Content(mediaType = "application/json")),
    })
    public ResponseEntity<?> pushMessage(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestBody RequestFcmTopicDto requestFcmTopicDto
    ) {
        if (accessToken == null) {
            throw new UnAuthorizedException("You must be requested to access token.");
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);

        fcmService.sendTopicNotification(requestFcmTopicDto);
        return ResponseEntity.noContent().build();
    }
}

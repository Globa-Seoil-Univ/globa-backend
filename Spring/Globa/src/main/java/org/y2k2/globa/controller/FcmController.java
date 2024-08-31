package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.dto.ResponseNotificationDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.FcmService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/fcm")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Fcm", description = "Firebase Cloud Messaging을 사용하여 알림을 보내는 API입니다.")
public class FcmController {
    private final FcmService fcmService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping("/send")
    @Operation(
            summary = "특정 토픽 알림 전송",
            description = "특정 토픽에게 알림을 보냅니다. 공지사항, 이벤트 알림에서 사용할 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "알림 보내기 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_NULL_ROLE, ref = SwaggerErrorCode.NOT_NULL_ROLE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_FCM, ref = SwaggerErrorCode.NOT_DESERVE_FCM_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FCM_SEND, ref = SwaggerErrorCode.FAILED_FCM_SEND_VALUE),
                    }))
            }
    )
    public ResponseEntity<?> pushMessage(
            @Parameter(hidden = true)
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestBody RequestFcmTopicDto requestFcmTopicDto
    ) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }

        fcmService.sendTopicNotification(accessToken, requestFcmTopicDto);
        return ResponseEntity.noContent().build();
    }
}

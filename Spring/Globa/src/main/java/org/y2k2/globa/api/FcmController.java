package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
import org.y2k2.globa.application.fcm.service.PushMessageService;
import org.y2k2.globa.application.fcm.service.SubscribeTopicService;
import org.y2k2.globa.application.fcm.service.UnSubscribeTopicService;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

@RestController
@RequestMapping("/fcm")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Fcm", description = "Firebase Cloud Messaging을 사용하여 알림을 보내는 API입니다.")
public class FcmController {
    private final PushMessageService pushMessageService;
    private final SubscribeTopicService subscribeTopicService;
    private final UnSubscribeTopicService unSubscribeTopicService;

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
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_PERMISSION, ref = SwaggerErrorCode.NOT_PERMISSION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
            }
    )
    public ResponseEntity<Void> pushMessage(
            @Valid @RequestBody RequestFcmTopicDto requestFcmTopicDto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        pushMessageService.send(requestFcmTopicDto, details.getUserId());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/topic")
    @Operation(
            summary = "특정 토픽 가입",
            description = "특정 토픽에 가입합니다. 해당 토픽에 알림을 받을 수 있습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "토픽 가입 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_TOKEN, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_TOKEN_VALUE),
                    })),
            }
    )
    public ResponseEntity<Void> subscribeTopic(
            @Valid @RequestBody RequestSubscribeTopicDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        subscribeTopicService.subscribe(dto, details.getUserId());
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/topic")
    @Operation(
            summary = "특정 토픽 탈퇴",
            description = "특정 토픽에서 탈퇴합니다. 해당 토픽에 알림을 받지 않습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "토픽 탈퇴 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_TOKEN, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_TOKEN_VALUE),
                    })),
            }
    )
    public ResponseEntity<Void> unsubscribeTopic(
            @Valid @RequestBody RequestSubscribeTopicDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        unSubscribeTopicService.unsubscribe(dto, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}

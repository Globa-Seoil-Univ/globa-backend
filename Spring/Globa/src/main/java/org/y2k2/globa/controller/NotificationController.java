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
import org.y2k2.globa.dto.ResponseInquiryDto;
import org.y2k2.globa.dto.ResponseNotificationDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.NotificationService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/notification")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API입니다.")
public class NotificationController {
    private final NotificationService notificationService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "알림 조회",
            description = "알림 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "알림 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseNotificationDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    public ResponseEntity<?> getNotifications(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "page", defaultValue = "1") int page) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        return ResponseEntity.ok().body(notificationService.getNotifications(userId, count, page));
    }
}

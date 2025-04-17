package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadCountDto;
import org.y2k2.globa.application.notification.dto.response.ResponseUnReadNotificationDto;
import org.y2k2.globa.application.notification.service.*;
import org.y2k2.globa.common.exception.SwaggerErrorCode;
import org.y2k2.globa.common.type.NotificationSort;

@RestController
@RequestMapping("/notification")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Notification", description = "알림 관련 API입니다.")
public class NotificationController {
    private final GetNotificationsService getNotificationsService;
    private final HasUnReadNotificationService hasUnReadNotificationService;
    private final GetUnReadCountNotificationService getUnReadCountNotificationService;
    private final ReadNotificationService readNotificationService;
    private final DeleteNotificationService deleteNotificationService;

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
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    public ResponseEntity<ResponseNotificationDto> getNotifications(
            @RequestParam(value = "type", defaultValue = "a") String type,
            @RequestParam(value = "count", defaultValue = "10") int count,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        NotificationSort sort = NotificationSort.valueOfString(type);
        return ResponseEntity.ok().body(
                getNotificationsService.get(count, page, sort, details.getUserId())
        );
    }

    @Operation(
            summary = "안 읽은 알림 여부 조회",
            description = "안 읽은 알림이 있는 여부를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "안 읽은 알림 여부 조회 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUnReadNotificationDto.class))
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
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/unread/check")
    public ResponseEntity<ResponseUnReadNotificationDto> getHasUnReadNotification(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        return ResponseEntity.ok().body(hasUnReadNotificationService.get(details.getUserId()));
    }

    @Operation(
            summary = "안 읽은 알림 개수 조회",
            description = "안 읽은 알림의 개수를 카테고리 별로 가져옵니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "안 읽은 알림 개수 조회 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUnReadCountDto.class))
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
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/unread/count")
    public ResponseEntity<ResponseUnReadCountDto> getCountUnReadNotification(
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        return ResponseEntity.ok().body(
                getUnReadCountNotificationService.get(details.getUserId())
        );
    }

    @Operation(
            summary = "알림 읽음 처리",
            description = "안 읽은 알림을 읽음 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "알림 읽음 처리 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/{notification_id}")
    public ResponseEntity<Void> readNotification(
            @PathVariable(value="notification_id") Long notificationId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        readNotificationService.read(notificationId, details.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "알림 삭제",
            description = """
                    알림을 삭제합니다. <br />
                    공지, 공유 파일 추가, 공유 사용자 추가, 공유 댓글 추가는 Soft Delete 처리됩니다. <br />
                    개인 알림 (업로드 완료, 실패, 문의, 공유 초대)은 삭제됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "알림 읽음 처리 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_VALUE),

                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/{notification_id}")
    public ResponseEntity<Void> deleteNotification(
            @PathVariable(value="notification_id") Long notificationId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        deleteNotificationService.delete(notificationId, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}

//package org.y2k2.globa.api;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.ExampleObject;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
//import org.y2k2.globa.application.notification.dto.response.ResponseNotificationDto;
//import org.y2k2.globa.application.notification.dto.response.ResponseUnreadCountDto;
//import org.y2k2.globa.application.notification.dto.response.ResponseUnreadNotificationDto;
//import org.y2k2.globa.common.exception.SwaggerErrorCode;
//import org.y2k2.globa.application.notification.service.NotificationService;
//import org.y2k2.globa.common.type.NotificationSort;
//
//@RestController
//@RequestMapping("/notification")
//@ResponseBody
//@RequiredArgsConstructor
//@Tag(name = "Notification", description = "알림 관련 API입니다.")
//public class NotificationController {
//    private final NotificationService notificationService;
//
//    @Operation(
//            summary = "알림 조회",
//            description = "알림 목록을 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "알림 목록 조회 성공",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseNotificationDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping
//    public ResponseEntity<?> getNotifications(
//            @RequestParam(value = "type", defaultValue = "a") String type,
//            @RequestParam(value = "count", defaultValue = "10") int count,
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        NotificationSort sort = NotificationSort.valueOfString(type);
//        return ResponseEntity.ok().body(notificationService.getNotifications(count, page, sort, details.getUser()));
//    }
//
//    @Operation(
//            summary = "안 읽은 알림 여부 조회",
//            description = "안 읽은 알림이 있는 여부를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "안 읽은 알림 여부 조회 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUnreadNotificationDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/unread/check")
//    public ResponseEntity<?> getHasUnreadNotifications(
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok().body(notificationService.getHasUnreadNotification(details.getUser()));
//    }
//
//    @Operation(
//            summary = "안 읽은 알림 개수 조회",
//            description = "안 읽은 알림의 개수를 카테고리 별로 가져옵니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "안 읽은 알림 개수 조회 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseUnreadCountDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/unread/count")
//    public ResponseEntity<?> getCountUnreadNotifications(
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok().body(notificationService.getCountUnreadNotification(details.getUser()));
//    }
//
//    @Operation(
//            summary = "알림 읽음 처리",
//            description = "안 읽은 알림을 읽음 처리합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "알림 읽음 처리 성공"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "409", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOTIFICATION_READ_DUPLICATED, ref = SwaggerErrorCode.NOTIFICATION_READ_DUPLICATED_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @PostMapping("/{notification_id}")
//    public ResponseEntity<?> readNotification(
//            @PathVariable(value="notification_id") Long notificationId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        notificationService.readNotification(notificationId, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            summary = "알림 삭제",
//            description = """
//                    알림을 삭제합니다. <br />
//                    공지, 공유 파일 추가, 공유 사용자 추가, 공유 댓글 추가는 Soft Delete 처리됩니다. <br />
//                    개인 알림 (업로드 완료, 실패, 문의, 공유 초대)은 삭제됩니다.
//                    """,
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "알림 읽음 처리 성공"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_NOTIFICATION_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTIFICATION, ref = SwaggerErrorCode.NOT_FOUND_NOTIFICATION_VALUE),
//
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @DeleteMapping("/{notification_id}")
//    public ResponseEntity<?> deleteNotification(
//            @PathVariable(value="notification_id") Long notificationId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        notificationService.deleteNotification(notificationId, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//}

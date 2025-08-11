package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.application.notice.service.CreateNoticeService;
import org.y2k2.globa.application.notice.service.GetIntroNoticesService;
import org.y2k2.globa.application.notice.service.GetNoticeDetailService;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/notice")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지 관련 API입니다.")
public class NoticeController {
    private final GetIntroNoticesService getIntroNoticesService;
    private final GetNoticeDetailService getNoticeDetailService;
    private final CreateNoticeService createNoticeService;

    @Operation(
            summary = "간단 공지사항 조회",
            description = "앱 메인화면에서 보여질 공지사항 3개를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공지사항 조회 성공",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = ResponseNoticeIntroDto.class)))
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
    @GetMapping("/intro")
    public ResponseEntity<ResponseNoticeIntroDto> getIntroNotices() {
        return ResponseEntity.ok().body(getIntroNoticesService.get());
    }

    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 상세 내용을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공지사항 상세 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseNoticeDetailDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTICE, ref = SwaggerErrorCode.NOT_FOUND_NOTICE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{noticeId}")
    public ResponseEntity<ResponseNoticeDetailDto> getNoticeDetail(@PathVariable("noticeId") Long noticeId) {
        return ResponseEntity.ok().body(getNoticeDetailService.get(noticeId));
    }

    @Operation(
            summary = "공지사항 추가",
            description = """
                    공지사항을 추가합니다. <br />
                    단, 공지사항은 관리자 또는 편집자만 추가할 수 있습니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "공지사항 추가 성공"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_PERMISSION, ref = SwaggerErrorCode.NOT_PERMISSION_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),

                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FILE_UPLOAD, ref = SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE),
                    }))
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> addNotice(
            @Valid @ModelAttribute final RequestNoticeAddDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        Long noticeId = createNoticeService.create(dto, details.getUserId());
        return ResponseEntity.created(URI.create("/notice/" + noticeId)).build();
    }
}
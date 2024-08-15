package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.NoticeAddRequestDto;
import org.y2k2.globa.dto.NoticeDetailResponseDto;
import org.y2k2.globa.dto.NoticeIntroResponseDto;
import org.y2k2.globa.dto.ResponseInquiryDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.NoticeService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/notice")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Notice", description = "공지 관련 API입니다.")
public class NoticeController {
    private final NoticeService noticeService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "간단 공지사항 조회",
            description = "앱 메인화면에서 보여질 공지사항 3개를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공지사항 조회 성공",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = NoticeIntroResponseDto.class)))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/intro")
    public ResponseEntity<?> getIntroNotices(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }

        return ResponseEntity.ok().body(noticeService.getIntroNotices());
    }

    @Operation(
            summary = "공지사항 상세 조회",
            description = "공지사항 상세 내용을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공지사항 상세 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = NoticeDetailResponseDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_NOTICE, ref = SwaggerErrorCode.NOT_FOUND_NOTICE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/{noticeId}")
    public ResponseEntity<?> getNoticeDetail(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken, @PathVariable("noticeId") Long noticeId) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        if (noticeId == null) {
            throw new CustomException(ErrorCode.REQUIRED_NOTICE_ID);
        }

        return ResponseEntity.ok().body(noticeService.getNoticeDetail(noticeId));
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE, ref = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),

                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addNotice(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken, @Valid @ModelAttribute final NoticeAddRequestDto dto) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        Long noticeId = noticeService.addNotice(userId, dto);
        return ResponseEntity.created(URI.create("/" + noticeId)).build();
    }
}
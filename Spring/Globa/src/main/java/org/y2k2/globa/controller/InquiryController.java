package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
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
import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.dto.request.inquiry.InquiryPaginationDto;
import org.y2k2.globa.dto.request.inquiry.RequestInquiryDto;
import org.y2k2.globa.dto.response.inquiry.ResponseInquiryDetailDto;
import org.y2k2.globa.dto.response.inquiry.ResponseInquiryDto;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.InquiryService;
import org.y2k2.globa.type.InquirySort;

import java.net.URI;

@RestController
@RequestMapping("/inquiry")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Inquiry", description = "문의 관련 API입니다.")
public class InquiryController {
    private final InquiryService inquiryService;

    @Operation(
            summary = "문의 조회",
            description = "문의 목록을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문의 목록 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseInquiryDto.class))
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
    public ResponseEntity<?> getInquires(
            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
            @RequestParam(value = "count", defaultValue = "100", required = false) int count,
            @RequestParam(value = "sort", defaultValue = "r", required = false) String sort,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        InquirySort inquirySort = InquirySort.from(sort);
        InquiryPaginationDto paginationDto = new InquiryPaginationDto(page, count, inquirySort);
        return ResponseEntity.ok().body(inquiryService.getInquiries(paginationDto, details.getUser()));
    }

    @Operation(
            summary = "문의 상세 조회",
            description = "문의 상세 내용을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문의 상세 조회 성공",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseInquiryDetailDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_INQUIRY_OWNER, ref = SwaggerErrorCode.MISMATCH_INQUIRY_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_INQUIRY, ref = SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ANSWER, ref = SwaggerErrorCode.NOT_FOUND_ANSWER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping(value = "/{inquiryId}")
    public ResponseEntity<?> getInquiry(
            @PathVariable(name = "inquiryId") long inquiryId,
            @AuthenticationPrincipal CustomUserDetails details
    ) { return ResponseEntity.ok().body(inquiryService.getInquiry(inquiryId, details.getUser())); }

    @Operation(
            summary = "문의 등록",
            description = "문의 내용을 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "문의 등록 성공"
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
    @PostMapping
    public ResponseEntity<?> addInquiry(
            @Valid @RequestBody RequestInquiryDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        long inquiryId = inquiryService.addInquiry(dto, details.getUser());
        return ResponseEntity.created(URI.create("/inquiry/" + inquiryId)).build();
    }
}

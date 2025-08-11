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
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.answer.service.CreateAnswerService;
import org.y2k2.globa.application.answer.service.DeleteAnswerService;
import org.y2k2.globa.application.answer.service.UpdateAnswerService;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

import java.net.URI;

@RestController
@RequestMapping("/inquiry/{inquiryId}")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Answer", description = "답변 관련 API입니다.")
public class AnswerController {
    private final CreateAnswerService createAnswerService;
    private final UpdateAnswerService updateAnswerService;
    private final DeleteAnswerService deleteAnswerService;

    @Operation(
            summary = "답변 추가",
            description = "문의에 답변을 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "답변 추가 성공"
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
                    @ApiResponse(responseCode = "409", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.INQUIRY_ANSWER_DUPLICATED, ref = SwaggerErrorCode.INQUIRY_ANSWER_DUPLICATED_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_INQUIRY, ref = SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/answer")
    public ResponseEntity<Void> addAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @Valid @RequestBody RequestAnswerDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        createAnswerService.create(inquiryId, dto, details.getUserId());
        return ResponseEntity.created(URI.create("/inquiry/" + inquiryId)).build();
    }

    @Operation(
            summary = "답변 수정",
            description = "문의에 답변을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "답변 수정 성공"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_INQUIRY, ref = SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ANSWER, ref = SwaggerErrorCode.NOT_FOUND_ANSWER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping(value = "/answer/{answerId}")
    public ResponseEntity<Void> editAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId,
            @Valid @RequestBody RequestAnswerDto dto,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        updateAnswerService.update(inquiryId, answerId, dto, details.getUserId());
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "답변 삭제",
            description = "문의에 답변을 삭제합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "답변 삭제 성공"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_INQUIRY, ref = SwaggerErrorCode.NOT_FOUND_INQUIRY_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ANSWER, ref = SwaggerErrorCode.NOT_FOUND_ANSWER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping(value = "/answer/{answerId}")
    public ResponseEntity<Void> deleteAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        deleteAnswerService.delete(inquiryId, answerId, details.getUserId());
        return ResponseEntity.noContent().build();
    }
}

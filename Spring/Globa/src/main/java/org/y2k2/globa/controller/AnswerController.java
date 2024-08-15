package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.y2k2.globa.dto.RequestAnswerDto;
import org.y2k2.globa.dto.ResponseInquiryDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.AnswerService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/inquiry/{inquiryId}")
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Answer", description = "답변 관련 API입니다.")
public class AnswerController {
    private final AnswerService answerService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(
            summary = "답변 추가",
            description = "문의에 답변을 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "답변 추가 성공"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE, ref = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping(value = "/answer")
    public ResponseEntity<?> addAnswer(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        answerService.addAnswer(userId, inquiryId, dto);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_NULL_ROLE, ref = SwaggerErrorCode.NOT_NULL_ROLE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE, ref = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE),
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
    public ResponseEntity<?> editAnswer(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        answerService.editAnswer(userId, inquiryId, answerId, dto);
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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_NULL_ROLE, ref = SwaggerErrorCode.NOT_NULL_ROLE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE, ref = SwaggerErrorCode.NOT_DESERVE_ADD_NOTICE_VALUE),
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
    public ResponseEntity<?> deleteAnswer(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId
    ) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);

        answerService.deleteAnswer(userId, inquiryId, answerId);
        return ResponseEntity.noContent().build();
    }
}

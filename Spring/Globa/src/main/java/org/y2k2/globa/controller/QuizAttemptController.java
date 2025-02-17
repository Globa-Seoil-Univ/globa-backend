package org.y2k2.globa.controller;

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
import org.y2k2.globa.dto.common.auth.CustomUserDetails;
import org.y2k2.globa.dto.request.quiz.RequestQuizDto;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.QuizAttemptService;

@RestController
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/folder/{folder_id}/record/{record_id}/quiz")
@Tag(name = "QuizAttempt", description = "퀴즈 시도 관련 API입니다.")
public class QuizAttemptController {
    private final QuizAttemptService quizAttemptService;

    @Operation(
            summary = "퀴즈 결과 추가",
            description = "문서에 대해 시도한 퀴즈 결과를 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "퀴즈 결과 추가 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID, ref = SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping
    public ResponseEntity<?> postQuiz(
            @PathVariable(value = "folder_id") Long folderId,
            @PathVariable(value = "record_id") Long recordId,
            @Valid @RequestBody RequestQuizDto quizzes,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        quizAttemptService.createQuizAttempts(recordId, folderId, quizzes, details.getUser());
        return ResponseEntity.noContent().build();
    }
}

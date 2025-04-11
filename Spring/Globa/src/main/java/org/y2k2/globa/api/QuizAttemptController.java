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
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.application.quizattemp.service.CreateQuizAttemptsService;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

@RestController
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/folder/{folder_id}/record/{record_id}/quiz")
@Tag(name = "QuizAttempt", description = "퀴즈 시도 관련 API입니다.")
public class QuizAttemptController {
    private final CreateQuizAttemptsService createQuizAttemptsService;

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
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_QUIZ, ref = SwaggerErrorCode.NOT_FOUND_QUIZ_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping
    public ResponseEntity<?> postQuiz(
            @PathVariable(value = "folder_id") Long folderId,
            @PathVariable(value = "record_id") Long recordId,
            @Valid @RequestBody final RequestQuizDto quizzes,
            @AuthenticationPrincipal CustomUserDetails details
    ) {
        createQuizAttemptsService.create(
                folderId,
                recordId,
                quizzes,
                details.getUserId()
        );
        return ResponseEntity.noContent().build();
    }
}

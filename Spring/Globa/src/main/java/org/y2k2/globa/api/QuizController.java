package org.y2k2.globa.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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
import org.y2k2.globa.application.quiz.service.QuizService;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.quiz.dto.common.QuizDto;
import org.y2k2.globa.common.exception.SwaggerErrorCode;

@RestController
@ResponseBody
@RequiredArgsConstructor
@RequestMapping("/folder/{folder_id}/record/{record_id}/quiz")
@Tag(name = "Quiz", description = "퀴즈 관련 API입니다.")
public class QuizController {
    private final QuizService quizService;

    @Operation(
            summary = "퀴즈 조회",
            description = "해당 폴더에 있는 녹음 파일에 대한 퀴즈를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "퀴즈 조회 완료",
                            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = QuizDto.class)))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_QUIZ, ref = SwaggerErrorCode.NOT_FOUND_QUIZ_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping
    public ResponseEntity<?> getQuiz(
            @PathVariable(value = "folder_id") Long folderId,
            @PathVariable(value = "record_id") Long recordId,
            @AuthenticationPrincipal CustomUserDetails details
    ) { return ResponseEntity.ok(quizService.getQuizzes(recordId, folderId, details.getUser())); }
}

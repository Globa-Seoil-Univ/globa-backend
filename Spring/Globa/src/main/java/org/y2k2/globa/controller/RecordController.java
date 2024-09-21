package org.y2k2.globa.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.SwaggerErrorCode;
import org.y2k2.globa.service.RecordService;

import java.util.List;
import java.util.Map;

@RestController
@ResponseBody
@RequiredArgsConstructor
@Tag(name = "Record", description = "음성 관련 API입니다.")
public class RecordController {
    private final RecordService recordService;

    @Operation(
            summary = "폴더 내 녹음 파일 조회",
            description = "폴더 내 녹음 파일을 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 내 녹음 파일 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordsByFolderDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/folder/{folder_id}/record")
    public ResponseEntity<?> getRecordByFolderId(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable(value = "folder_id", required = false) Long folderId,
                                                 @RequestParam(required = false, defaultValue = "1", value = "page") int page,
                                                 @RequestParam(required = false, defaultValue = "100", value = "count") int count) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getRecords(accessToken, folderId, page, count));
    }

    @Operation(
            summary = "모든 녹음 파일 조회",
            description = "폴더와 상관 없이 모든 문서에서 검색을 합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "녹음 파일 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAllRecordWithTotalDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/record")
    public ResponseEntity<?> getAllRecord(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @RequestParam(required = false, defaultValue = "100", value = "count") int count) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getAllRecords(accessToken, count));
    }

    @Operation(
            summary = "녹음 파일 상세 조회",
            description = "녹음 파일을 상세히 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "폴더 내 녹음 파일 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordDetailDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ANALYSIS, ref = SwaggerErrorCode.NOT_FOUND_ANALYSIS_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> getRecordDetail(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable(value = "folder_id", required = false) Long folderId,
                                                @PathVariable(value = "record_id", required = false) Long recordId) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null )
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getRecordDetail(accessToken, folderId, recordId));
    }

    @Operation(
            summary = "문서 내 시각화 자료 조회",
            description = "해당 폴더에 있는 녹음 파일에 대한 시각화 자료를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "시각화 자료 조회 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAnalysisDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/folder/{folder_id}/record/{record_id}/analysis")
    public ResponseEntity<?> getAnalysis(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                         @PathVariable(value = "folder_id", required = false) Long folderId,
                                         @PathVariable(value = "record_id", required = false) Long recordId) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null )
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        ResponseAnalysisDto result = recordService.getAnalysis(accessToken,recordId, folderId);

        return ResponseEntity.ok(result);
    }

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
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_QUIZ, ref = SwaggerErrorCode.NOT_FOUND_QUIZ_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> getQuiz(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                         @PathVariable(value = "folder_id", required = false) Long folderId,
                                         @PathVariable(value = "record_id", required = false) Long recordId) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null )
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        List<QuizDto> result = recordService.getQuiz(accessToken,recordId, folderId);

        return ResponseEntity.ok(result);
    }

    @Operation(
            summary = "문서 검색",
            description = "소유하고 있거나, 공유 받고 있는 모든 문서를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문서 조회 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordSearchDto.class))
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/record/search")
    public ResponseEntity<?> searchRecord(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "100") int count,
            @RequestParam(required = false) String keyword) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        return ResponseEntity.ok(recordService.searchRecord(accessToken, keyword, page, count));
    }

    @Operation(
            summary = "공유 받는 문서 조회",
            description = "공유 받고 있는 문서를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유 받는 문서 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAllRecordWithTotalDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/record/receiving")
    public ResponseEntity<?> getReceivingRecord(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "100") int count) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        return ResponseEntity.ok(recordService.getReceivingRecords(accessToken, page, count));
    }

    @Operation(
            summary = "공유 하는 문서 조회",
            description = "공유 하고 있는 문서를 조회합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공유 하는 문서 가져오기 완료",
                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAllRecordWithTotalDto.class))
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_ACCESSIBLE_FOLDER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @GetMapping("/record/sharing")
    public ResponseEntity<?> getSharingRecord(
            @Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "100") int count) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        return ResponseEntity.ok(recordService.getSharingRecords(accessToken, page, count));
    }

    @Operation(
            summary = "문서 링크 공유",
            description = """
                    해당 문서를 다른 사용자에게 공유합니다. <br />
                    문서를 공유하게 되면 링크를 가진 모든 사람이 접근 가능합니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "문서 링크 공유 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/folder/{folder_id}/record/{record_id}/link")
    public ResponseEntity<?> addLinkShare(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                      @PathVariable(value = "record_id", required = false) Long recordId) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if (folderId == null) throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if (recordId == null) throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.changeLinkShare(accessToken, folderId, recordId, true);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "퀴즈 결과 추가",
            description = "문서에 대해 시도한 퀴즈 결과를 추가합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "퀴즈 결과 추가 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_QUIZ_ID, ref = SwaggerErrorCode.REQUIRED_QUIZ_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID, ref = SwaggerErrorCode.MISMATCH_QUIZ_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> postQuiz(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                     @PathVariable(value = "record_id", required = false) Long recordId,
                                      @RequestBody RequestQuizWrapper quizs) {

        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null )
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);
        if ( quizs.getQuizs() == null )
            throw new CustomException(ErrorCode.REQUIRED_QUIZ);


        return ResponseEntity.status(recordService.postQuiz(accessToken,recordId, folderId, quizs.getQuizs())).body("");
    }

    @Operation(
            summary = "문서 추가",
            description = """
                   폴더에 녹음 파일을 추가합니다. <br />
                   해당 녹음 파일은 클라이언트에서 Firebase Storage 업로드 후 요청되어야 합니다. <br />
                   또한, Kafka를 통해 Python 서버에서 STT, 퀴즈 생성, 키워드 추출, 섹션 분리, 섹션 요약이 실행되는 작업입니다. <br />
                   모든 분석이 끝나기까지 녹음 파일의 길이에 따라 5 ~ 30분 정도 소요됩니다.
                    """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문서 추가 및 분석 요청 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.RECORD_POST_BAD_REQUEST, ref = SwaggerErrorCode.RECORD_POST_BAD_REQUEST_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PostMapping("/folder/{folder_id}/record")
    public ResponseEntity<?> postRecord(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                      @RequestBody RequestPostRecordDto requestPostRecordDto) {

        if (accessToken == null)
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( requestPostRecordDto.getPath() == null || requestPostRecordDto.getTitle() == null || requestPostRecordDto.getSize() == null)
            throw new CustomException(ErrorCode.RECORD_POST_BAD_REQUEST);


        return ResponseEntity.status(recordService.postRecord(accessToken, folderId, requestPostRecordDto.getTitle(),requestPostRecordDto.getPath(), requestPostRecordDto.getSize())).body("");
    }

    @Operation(
            summary = "문서 이름 수정",
            description = "문서 이름을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문서 이름 수정 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_TITLE, ref = SwaggerErrorCode.REQUIRED_RECORD_TITLE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping("/folder/{folder_id}/record/{record_id}/name")
    public ResponseEntity<?> patchRecordName(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                             @PathVariable(value = "folder_id", required = false) Long folderId,
                                        @PathVariable(value = "record_id", required = false) Long recordId,
                                        @RequestBody Map<String, String> titleMap){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( titleMap.get("title") == null  )
            throw new CustomException(ErrorCode.REQUIRED_RECORD_TITLE);
        if ( recordId == null)
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        return ResponseEntity.status(recordService.patchRecordName(accessToken, recordId, folderId, titleMap.get("title"))).body("");
    }

    @Operation(
            summary = "문서 폴더 이동",
            description = "문서를 다른 폴더로 이동합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "문서 이동 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_MOVE_ARRIVED_ID, ref = SwaggerErrorCode.REQUIRED_MOVE_ARRIVED_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_ORIGIN_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_ORIGIN_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.FAILED_FIREBASE, ref = SwaggerErrorCode.FAILED_FIREBASE_VALUE)
                    }))
            }
    )
    @PatchMapping("/folder/{folder_id}/record/{record_id}/folder")
    public ResponseEntity<?> patchRecordMove(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                             @PathVariable(value = "folder_id", required = false) Long folderId,
                                             @PathVariable(value = "record_id", required = false) Long recordId,
                                             @RequestBody Map<String, String> targetIdMap){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( targetIdMap.get("targetId") == null  )
            throw new CustomException(ErrorCode.REQUIRED_MOVE_ARRIVED_ID);
        if ( recordId == null)
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        return ResponseEntity.status(recordService.patchRecordMove(accessToken, recordId, folderId, Long.valueOf(targetIdMap.get("targetId")))).body("");
    }

    @Operation(
            summary = "공부 시간 수정",
            description = "공부 시간을 수정합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "공부 시간 수정 완료"
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
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_FOLDER, ref = SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @PatchMapping("/folder/{folder_id}/record/{record_id}/study")
    public ResponseEntity<?> patchStudyTime(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
                                            @PathVariable(value = "folder_id") Long folderId,
                                            @PathVariable(value = "record_id") Long recordId,
                                            @RequestBody RequestStudyDto dto) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        recordService.patchStudyTime(accessToken, recordId, folderId, dto);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "문서 삭제",
            description = "문서를 삭제를 하며, 해당 작업은 다시 되돌릴 수 없습니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "문서 삭제 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_FOLDER, ref = SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE)
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> deleteFolder(@Parameter(hidden=true) @RequestHeader(value = "Authorization") String accessToken,
                                          @PathVariable(value = "record_id") Long recordId,
                                          @PathVariable(value = "folder_id") Long folderId){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null)
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.deleteRecord(accessToken, recordId, folderId);

        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "문서 링크 공유 취소",
            description = "링크 공유한 문서를 취소합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "문서 링크 공유 취소 완료"
                    ),
                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.REQUIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_FOLDER_ID, ref = SwaggerErrorCode.REQUIRED_FOLDER_ID_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.REQUIRED_RECORD_ID, ref = SwaggerErrorCode.REQUIRED_RECORD_ID_VALUE),
                    })),
                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
                    })),
                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
                    })),
                    @ApiResponse(responseCode = "500", ref = "500")
            }
    )
    @DeleteMapping("/folder/{folder_id}/record/{record_id}/link")
    public ResponseEntity<?> deleteLinkShare(@Parameter(hidden=true) @RequestHeader(value = "Authorization", required = false) String accessToken,
                                          @PathVariable(value = "folder_id", required = false) Long folderId,
                                          @PathVariable(value = "record_id", required = false) Long recordId) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if (folderId == null) throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if (recordId == null) throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.changeLinkShare(accessToken, folderId, recordId, false);
        return ResponseEntity.noContent().build();
    }
}

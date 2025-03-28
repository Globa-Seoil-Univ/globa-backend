//package org.y2k2.globa.api;
//
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.ExampleObject;
//import io.swagger.v3.oas.annotations.media.Schema;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
//import io.swagger.v3.oas.annotations.tags.Tag;
//import jakarta.validation.Valid;
//import lombok.RequiredArgsConstructor;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.annotation.AuthenticationPrincipal;
//import org.springframework.web.bind.annotation.*;
//import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
//import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
//import org.y2k2.globa.application.record.dto.request.RequestRecordMoveDto;
//import org.y2k2.globa.application.record.dto.request.RequestRecordNameDto;
//import org.y2k2.globa.application.analysis.dto.response.ResponseAnalysisDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordDetailDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordsByFolderDto;
//import org.y2k2.globa.application.studytime.dto.request.RequestStudyDto;
//import org.y2k2.globa.application.record.dto.response.ResponseRecordsDto;
//import org.y2k2.globa.common.exception.SwaggerErrorCode;
//import org.y2k2.globa.application.record.service.RecordService;
//
//@RestController
//@ResponseBody
//@RequiredArgsConstructor
//@Tag(name = "Record", description = "문서 관련 API입니다.")
//public class RecordController {
//    private final RecordService recordService;
//
//    @Operation(
//            summary = "폴더 내 문서 조회",
//            description = "폴더 내 문서를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "폴더 내 문서 가져오기 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordsByFolderDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/folder/{folder_id}/record")
//    public ResponseEntity<?> getRecordByFolderId(
//            @PathVariable(value = "folder_id") Long folderId,
//            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
//            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.getRecords(folderId, page, count, details.getUser()));
//    }
//
//    @Operation(
//            summary = "최신 문서 조회",
//            description = "폴더와 상관 없이 최신 문서를 가져옵니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "문서 목록 가져오기 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordsDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/record")
//    public ResponseEntity<?> getRecentRecords(
//            @RequestParam(value = "page", defaultValue = "1") int page,
//            @RequestParam(value = "count", defaultValue = "10") int count,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.getRecentRecords(page, count, details.getUser()));
//    }
//
//    @Operation(
//            summary = "문서 상세 조회",
//            description = "문서를 상세히 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "폴더 내 문서 가져오기 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordDetailDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/folder/{folder_id}/record/{record_id}")
//    public ResponseEntity<?> getRecordDetail(
//            @PathVariable(value = "folder_id") Long folderId,
//            @PathVariable(value = "record_id") Long recordId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.getRecordDetail(folderId, recordId, details.getUser()));
//    }
//
//    @Operation(
//            summary = "문서 내 시각화 자료 조회",
//            description = "해당 폴더에 있는 문서에 대한 시각화 자료를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "시각화 자료 조회 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseAnalysisDto.class))
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @GetMapping("/folder/{folder_id}/record/{record_id}/analysis")
//    public ResponseEntity<?> getAnalysis(
//                    @PathVariable(value = "folder_id") Long folderId,
//                    @PathVariable(value = "record_id") Long recordId,
//                    @AuthenticationPrincipal CustomUserDetails details
//    ) { return ResponseEntity.ok(recordService.getAnalysis(recordId, folderId, details.getUser())); }
//
//    @Operation(
//            summary = "문서 검색",
//            description = "소유하고 있거나, 공유 받고 있는 모든 문서를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "문서 조회 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordSearchDto.class))
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
//    @GetMapping("/record/search")
//    public ResponseEntity<?> searchRecord(
//            @RequestParam(value = "page", defaultValue = "1", required = false) int page,
//            @RequestParam(value = "count", defaultValue = "10", required = false) int count,
//            @RequestParam(required = false) String keyword,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.searchRecord(keyword, page, count, details.getUser()));
//    }
//
//    @Operation(
//            summary = "공유 받는 문서 조회",
//            description = "공유 받고 있는 문서를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "공유 받는 문서 가져오기 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordsDto.class))
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
//    @GetMapping("/record/receiving")
//    public ResponseEntity<?> getReceivingRecord(
//            @RequestParam(required = false, defaultValue = "1") int page,
//            @RequestParam(required = false, defaultValue = "10") int count,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.getReceivingRecords(page, count, details.getUser()));
//    }
//
//    @Operation(
//            summary = "공유 하는 문서 조회",
//            description = "공유 하고 있는 문서를 조회합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "공유 하는 문서 가져오기 완료",
//                            content = @Content(mediaType = "application/json", schema = @Schema(implementation = ResponseRecordsDto.class))
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
//    @GetMapping("/record/sharing")
//    public ResponseEntity<?> getSharingRecord(
//            @RequestParam(required = false, defaultValue = "1") int page,
//            @RequestParam(required = false, defaultValue = "10") int count,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        return ResponseEntity.ok(recordService.getSharingRecords(page, count, details.getUser()));
//    }
//
//    @Operation(
//            summary = "문서 링크 공유",
//            description = """
//                    해당 문서를 다른 사용자에게 공유합니다. <br />
//                    문서를 공유하게 되면 링크를 가진 모든 사람이 접근 가능합니다. <br />
//                    단, 링크를 통해 접근한 사용자는 문서를 수정할 수 없습니다.
//                    """,
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "문서 링크 공유 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @PostMapping("/folder/{folder_id}/record/{record_id}/link")
//    public ResponseEntity<?> addLinkShare(
//            @PathVariable(value = "folder_id", required = false) Long folderId,
//            @PathVariable(value = "record_id", required = false) Long recordId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.changeLinkShare(folderId, recordId, true, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            deprecated = true,
//            summary = "문서 추가",
//            description = """
//                   폴더에 녹음 파일을 추가합니다. <br />
//                   해당 녹음 파일은 클라이언트에서 Firebase Storage 업로드 후 요청되어야 합니다. <br />
//                   또한, Kafka를 통해 Python 서버에서 STT, 퀴즈 생성, 키워드 추출, 섹션 분리, 섹션 요약이 실행되는 작업입니다. <br />
//                   모든 분석이 끝나기까지 녹음 파일의 길이에 따라 5 ~ 30분 정도 소요됩니다.
//                    """,
//            responses = {
//                    @ApiResponse(
//                            responseCode = "200",
//                            description = "문서 추가 및 분석 요청 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @PostMapping("/folder/{folder_id}/record")
//    public ResponseEntity<?> createdRecord(
//                            @PathVariable(value = "folder_id") Long folderId,
//                            @Valid @RequestBody RequestPostRecordDto dto,
//                            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.createdRecord(folderId, dto, details.getUser());
//        return ResponseEntity.ok().build();
//    }
//
//    @Operation(
//            summary = "문서 이름 수정",
//            description = "문서 이름을 수정합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "문서 이름 수정 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @PatchMapping("/folder/{folder_id}/record/{record_id}/name")
//    public ResponseEntity<?> modifyRecordName(
//            @PathVariable(value = "folder_id") Long folderId,
//            @PathVariable(value = "record_id") Long recordId,
//            @Valid @RequestBody RequestRecordNameDto dto,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.modifyRecordName(folderId, recordId, dto, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            summary = "문서 폴더 이동",
//            description = "문서를 다른 폴더로 이동합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "문서 이동 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE)
//
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_TARGET_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE, ref = SwaggerErrorCode.NOT_FOUND_RECORD_FIREBASE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.FAILED_FILE_UPLOAD, ref = SwaggerErrorCode.FAILED_FILE_UPLOAD_VALUE)
//                    }))
//            }
//    )
//    @PatchMapping("/folder/{folder_id}/record/{record_id}/move")
//    public ResponseEntity<?> moveRecord(
//            @PathVariable(value = "folder_id") Long folderId,
//            @PathVariable(value = "record_id") Long recordId,
//            @Valid @RequestBody RequestRecordMoveDto dto,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.moveRecord(folderId, recordId, dto, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            summary = "공부 시간 수정",
//            description = "공부 시간을 수정합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "공부 시간 수정 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_FOLDER, ref = SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @PatchMapping("/folder/{folder_id}/record/{record_id}/study")
//    public ResponseEntity<?> modifyStudyTime(
//            @PathVariable(value = "folder_id") Long folderId,
//            @PathVariable(value = "record_id") Long recordId,
//            @Valid @RequestBody RequestStudyDto dto,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.modifyStudyTime(folderId, recordId, dto, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            summary = "문서 삭제",
//            description = "문서를 삭제를 하며, 해당 작업은 다시 되돌릴 수 없습니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "문서 삭제 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "401", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER, ref = SwaggerErrorCode.NOT_DESERVE_ACCESS_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_FOLDER, ref = SwaggerErrorCode.MISMATCH_RECORD_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @DeleteMapping("/folder/{folder_id}/record/{record_id}")
//    public ResponseEntity<?> deleteFolder(
//            @PathVariable(value = "record_id") Long recordId,
//            @PathVariable(value = "folder_id") Long folderId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.deleteRecord(folderId, recordId, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//
//    @Operation(
//            summary = "문서 링크 공유 취소",
//            description = "링크 공유한 문서를 취소합니다.",
//            responses = {
//                    @ApiResponse(
//                            responseCode = "204",
//                            description = "문서 링크 공유 취소 완료"
//                    ),
//                    @ApiResponse(responseCode = "400", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN, ref = SwaggerErrorCode.EXPIRED_ACCESS_TOKEN_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.SIGNATURE, ref = SwaggerErrorCode.SIGNATURE_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_FOLDER_OWNER, ref = SwaggerErrorCode.MISMATCH_FOLDER_OWNER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.MISMATCH_RECORD_OWNER, ref = SwaggerErrorCode.MISMATCH_RECORD_OWNER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "403", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.DELETED_USER, ref = SwaggerErrorCode.DELETED_USER_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "404", content = @Content(mediaType = MediaType.APPLICATION_JSON_VALUE, examples = {
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_USER, ref = SwaggerErrorCode.NOT_FOUND_USER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_FOLDER, ref = SwaggerErrorCode.NOT_FOUND_FOLDER_VALUE),
//                            @ExampleObject(name = SwaggerErrorCode.NOT_FOUND_RECORD, ref = SwaggerErrorCode.NOT_FOUND_RECORD_VALUE),
//                    })),
//                    @ApiResponse(responseCode = "500", ref = "500")
//            }
//    )
//    @DeleteMapping("/folder/{folder_id}/record/{record_id}/link")
//    public ResponseEntity<?> deleteLinkShare(
//            @PathVariable(value = "folder_id") Long folderId,
//            @PathVariable(value = "record_id") Long recordId,
//            @AuthenticationPrincipal CustomUserDetails details
//    ) {
//        recordService.changeLinkShare(folderId, recordId, false, details.getUser());
//        return ResponseEntity.noContent().build();
//    }
//}

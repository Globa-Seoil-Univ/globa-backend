package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.RecordService;

import java.util.List;
import java.util.Map;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class RecordController {
    private final RecordService recordService;

    @GetMapping("/folder/{folder_id}/record")
    public ResponseEntity<?> getRecordByFolderId(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable(value = "folder_id", required = false) Long folderId,
                                                 @RequestParam(required = false, defaultValue = "1", value = "page") int page,
                                                 @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null )
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getRecords(accessToken, folderId, page, count));
    }

    @GetMapping("/record")
    public ResponseEntity<?> getAllRecord(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getAllRecords(accessToken, count));
    }

    @GetMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> getRecordDetail(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @GetMapping("/folder/{folder_id}/record/{record_id}/analysis")
    public ResponseEntity<?> getAnalysis(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @GetMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> getQuiz(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @PostMapping("/folder/{folder_id}/record/{record_id}/link")
    public ResponseEntity<?> addLinkShare(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                      @PathVariable(value = "record_id", required = false) Long recordId) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if (folderId == null) throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if (recordId == null) throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.changeLinkShare(accessToken, folderId, recordId, true);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> postQuiz(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @PostMapping("/folder/{folder_id}/record")
    public ResponseEntity<?> postRecord(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @PatchMapping("/folder/{folder_id}/record/{record_id}/name")
    public ResponseEntity<?> patchRecordName(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @PatchMapping("/folder/{folder_id}/record/{record_id}/folder")
    public ResponseEntity<?> patchRecordMove(@RequestHeader(value = "Authorization", required = false) String accessToken,
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

    @PatchMapping("/folder/{folder_id}/record/{record_id}/study")
    public ResponseEntity<?> patchStudyTime(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                            @PathVariable(value = "folder_id", required = false) Long folderId,
                                            @PathVariable(value = "record_id", required = false) Long recordId,
                                            @RequestBody RequestStudyDto dto) {
        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);

        recordService.patchStudyTime(accessToken, recordId, folderId, dto);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> deleteFolder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                          @PathVariable(value = "record_id", required = false) Long recordId,
                                          @PathVariable(value = "folder_id", required = false) Long folderId){


        if ( accessToken == null )
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if ( folderId == null)
            throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if ( recordId == null)
            throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.deleteRecord(accessToken, recordId, folderId);

        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/folder/{folder_id}/record/{record_id}/link")
    public ResponseEntity<?> deleteLinkShare(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                          @PathVariable(value = "folder_id", required = false) Long folderId,
                                          @PathVariable(value = "record_id", required = false) Long recordId) {
        if (accessToken == null) throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        if (folderId == null) throw new CustomException(ErrorCode.REQUIRED_FOLDER_ID);
        if (recordId == null) throw new CustomException(ErrorCode.REQUIRED_RECORD_ID);

        recordService.changeLinkShare(accessToken, folderId, recordId, false);
        return ResponseEntity.noContent().build();
    }
}

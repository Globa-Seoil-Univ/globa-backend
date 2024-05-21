package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.FolderShareService;
import org.y2k2.globa.service.RecordService;
import org.y2k2.globa.util.JwtToken;

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
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required Folder ID ! ");


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getRecords(accessToken, folderId, page, count));
    }

    @GetMapping("/record")
    public ResponseEntity<?> getAllRecord(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @RequestParam(required = false, defaultValue = "10", value = "count") int count) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getAllRecords(accessToken, count));
    }

    @GetMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> getRecordDetail(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                                 @PathVariable(value = "folder_id", required = false) Long folderId,
                                                @PathVariable(value = "record_id", required = false) Long recordId) {
        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required Folder ID ! ");
        if ( recordId == null )
            throw new BadRequestException("Required recordID ! ");


        return ResponseEntity.status(HttpStatus.OK).body(recordService.getRecordDetail(accessToken, folderId, recordId));
    }

    @GetMapping("/folder/{folder_id}/record/{record_id}/analysis")
    public ResponseEntity<?> getAnalysis(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                         @PathVariable(value = "folder_id", required = false) Long folderId,
                                         @PathVariable(value = "record_id", required = false) Long recordId) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required folderId ! ");
        if ( recordId == null )
            throw new BadRequestException("Required recordId ! ");

        ResponseAnalysisDto result = recordService.getAnalysis(accessToken,recordId, folderId);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> getQuiz(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                         @PathVariable(value = "folder_id", required = false) Long folderId,
                                         @PathVariable(value = "record_id", required = false) Long recordId) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required folderId ! ");
        if ( recordId == null )
            throw new BadRequestException("Required recordId ! ");

        List<QuizDto> result = recordService.getQuiz(accessToken,recordId, folderId);

        return ResponseEntity.ok(result);
    }

    @PostMapping("/folder/{folder_id}/record/{record_id}/quiz")
    public ResponseEntity<?> postQuiz(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                     @PathVariable(value = "record_id", required = false) Long recordId,
                                      @RequestBody RequestQuizWrapper quizs) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required folderId ! ");
        if ( recordId == null )
            throw new BadRequestException("Required recordId ! ");
        if ( quizs.getQuizs() == null )
            throw new BadRequestException("Required quizs ! ");


        return ResponseEntity.status(recordService.postQuiz(accessToken,recordId, folderId, quizs.getQuizs())).body("");
    }

    @PostMapping("/folder/{folder_id}/record")
    public ResponseEntity<?> postRecord(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                      @PathVariable(value = "folder_id", required = false) Long folderId,
                                      @RequestBody RequestPostRecordDto requestPostRecordDto) {

        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null )
            throw new BadRequestException("Required folderId ! ");
        if ( requestPostRecordDto.getPath() == null || requestPostRecordDto.getTitle() == null || requestPostRecordDto.getSize() == null)
            throw new BadRequestException("requestPostRecordDto included value required ! ");


        return ResponseEntity.status(recordService.postRecord(accessToken, folderId, requestPostRecordDto.getTitle(),requestPostRecordDto.getPath(), requestPostRecordDto.getSize())).body("");
    }

    @PatchMapping("/folder/{folder_id}/record/{record_id}/name")
    public ResponseEntity<?> patchRecordName(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                             @PathVariable(value = "folder_id", required = false) Long folderId,
                                        @PathVariable(value = "record_id", required = false) Long recordId,
                                        @RequestBody Map<String, String> titleMap){


        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( titleMap.get("title") == null  )
            throw new BadRequestException("Title Value, Null Not Allowed ! ");
        if ( recordId == null)
            throw new BadRequestException("recordId Path Value, Null Not Allowed ! ");

        return ResponseEntity.status(recordService.patchRecordName(accessToken, recordId, folderId, titleMap.get("title"))).body("");
    }

    @PatchMapping("/folder/{folder_id}/record/{record_id}/folder")
    public ResponseEntity<?> patchRecordMove(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                             @PathVariable(value = "folder_id", required = false) Long folderId,
                                             @PathVariable(value = "record_id", required = false) Long recordId,
                                             @RequestBody Map<String, String> targetIdMap){


        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( targetIdMap.get("targetId") == null  )
            throw new BadRequestException("TargetId Value, Null Not Allowed ! ");
        if ( recordId == null)
            throw new BadRequestException("recordId Path Value, Null Not Allowed ! ");

        return ResponseEntity.status(recordService.patchRecordMove(accessToken, recordId, folderId, Long.valueOf(targetIdMap.get("targetId")))).body("");
    }

    @DeleteMapping("/folder/{folder_id}/record/{record_id}")
    public ResponseEntity<?> deleteFolder(@RequestHeader(value = "Authorization", required = false) String accessToken,
                                          @PathVariable(value = "record_id", required = false) Long recordId,
                                          @PathVariable(value = "folder_id", required = false) Long folderId){


        if ( accessToken == null )
            throw new BadRequestException("Required AccessToken ! ");
        if ( folderId == null)
            throw new BadRequestException("folderId Path Value, Null Not Allowed ! ");
        if ( recordId == null)
            throw new BadRequestException("recordId Path Value, Null Not Allowed ! ");

        return ResponseEntity.status(recordService.deleteRecord(accessToken, recordId, folderId)).body("");
    }
}

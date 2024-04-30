package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestAnswerDto;
import org.y2k2.globa.service.AnswerService;
import org.y2k2.globa.service.InquiryService;

import java.net.URI;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class AnswerController {
    private final String PRE_FIX = "/inquiry/{inquiryId}";
    private final AnswerService answerService;

    @PostMapping(value = PRE_FIX + "/answer")
    public ResponseEntity<?> addAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        // token 체크

        answerService.addAnswer(1L, inquiryId, dto);
        return ResponseEntity.created(URI.create("/inquiry/" + inquiryId)).build();
    }

    @PatchMapping(value = PRE_FIX + "/answer/{answerId}")
    public ResponseEntity<?> editAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        // token 체크

        answerService.editAnswer(1L, inquiryId, answerId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = PRE_FIX + "/answer/{answerId}")
    public ResponseEntity<?> deleteAnswer(
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId
    ) {
        // token 체크

        answerService.deleteAnswer(1L, inquiryId, answerId);
        return ResponseEntity.noContent().build();
    }
}

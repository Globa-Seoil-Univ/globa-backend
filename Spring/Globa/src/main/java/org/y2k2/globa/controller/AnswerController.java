package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.RequestAnswerDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.AnswerService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/inquiry/{inquiryId}")
@ResponseBody
@RequiredArgsConstructor
public class AnswerController {
    private final AnswerService answerService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(value = "/answer")
    public ResponseEntity<?> addAnswer(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        answerService.addAnswer(userId, inquiryId, dto);
        return ResponseEntity.created(URI.create("/inquiry/" + inquiryId)).build();
    }

    @PatchMapping(value = "/answer/{answerId}")
    public ResponseEntity<?> editAnswer(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId,
            @Valid @RequestBody RequestAnswerDto dto
    ) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        answerService.editAnswer(userId, inquiryId, answerId, dto);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping(value = "/answer/{answerId}")
    public ResponseEntity<?> deleteAnswer(
            @RequestHeader(value = "Authorization") String accessToken,
            @PathVariable("inquiryId") long inquiryId,
            @PathVariable("answerId") long answerId
    ) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        answerService.deleteAnswer(userId, inquiryId, answerId);
        return ResponseEntity.noContent().build();
    }
}

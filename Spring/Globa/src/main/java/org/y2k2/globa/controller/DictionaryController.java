package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.service.DictionaryService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/dictionary")
@ResponseBody
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<?> getDictionary(@RequestHeader(value = "Authorization") String accessToken, @RequestParam(required = true, value = "keyword") String keyword) {
        if (accessToken == null) {
            throw new CustomException(ErrorCode.REQUIRED_ACCESS_TOKEN);
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);
        return ResponseEntity.ok().body(dictionaryService.getDictionary(keyword));
    }
}

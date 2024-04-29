package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.service.DictionaryService;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class DictionaryController {
    private final String PRE_FIX = "/dictionary";
    private final DictionaryService dictionaryService;

    @GetMapping(value = PRE_FIX)
    public ResponseEntity<?> getDictionary(@RequestParam(required = true, value = "keyword") String keyword) {
        // token 체크
        return ResponseEntity.ok().body(dictionaryService.getDictionary(keyword));
    }
}

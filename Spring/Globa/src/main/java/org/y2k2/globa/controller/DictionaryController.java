package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.service.DictionaryService;

@RestController
@RequestMapping("/dictionary")
@ResponseBody
@RequiredArgsConstructor
public class DictionaryController {
    private final DictionaryService dictionaryService;

    @GetMapping
    public ResponseEntity<?> getDictionary(@RequestParam(value = "keyword") String keyword) {
        ResponseDictionaryDto dto = dictionaryService.getDictionary(keyword);
        return ResponseEntity.status(HttpStatus.OK).body(dto);
    }

    @PostMapping
    public ResponseEntity<?> saveDictionary() {
        dictionaryService.saveDictionary();
        return ResponseEntity.noContent().build();
    }
}

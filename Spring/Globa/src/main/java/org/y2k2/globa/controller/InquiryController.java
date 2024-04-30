package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.dto.RequestInquiryDto;
import org.y2k2.globa.service.InquiryService;

import java.net.URI;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class InquiryController {
    private final String PRE_FIX = "/inquiry";
    private final InquiryService inquiryService;

    @PostMapping(value = PRE_FIX)
    public ResponseEntity<?> addInquiry(@Valid @RequestBody RequestInquiryDto dto) {
        // token 체크

        long inquiryId = inquiryService.addInquiry(1L, dto);
        return ResponseEntity.created(URI.create(PRE_FIX + "/" + inquiryId)).build();
    }
}

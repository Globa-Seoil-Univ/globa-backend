package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.service.InquiryService;

import java.net.URI;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class InquiryController {
    private final String PRE_FIX = "/inquiry";
    private final InquiryService inquiryService;

    @GetMapping(value = PRE_FIX)
    public ResponseEntity<?> getInquires(
            @RequestParam(required = false, value = "page", defaultValue = "1") int page,
            @RequestParam(required = false, value = "count", defaultValue = "10") int count,
            @RequestParam(required = false, value = "sort", defaultValue = "r") String sort
    ) {
        // token 체크

        SortDto sortDto = SortDto.valueOfString(sort);
        PaginationDto paginationDto = new PaginationDto(page, count, sortDto);
        ResponseInquiryDto dto = inquiryService.getInquiries(1L, paginationDto);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping(value = PRE_FIX + "/{inquiryId}")
    public ResponseEntity<?> getInquiry(@PathVariable(name = "inquiryId") long inquiryId) {
        // token 체크

        ResponseInquiryDetailDto dto = inquiryService.getInquiry(1L, inquiryId);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping(value = PRE_FIX)
    public ResponseEntity<?> addInquiry(@Valid @RequestBody RequestInquiryDto dto) {
        // token 체크

        long inquiryId = inquiryService.addInquiry(1L, dto);
        return ResponseEntity.created(URI.create(PRE_FIX + "/" + inquiryId)).build();
    }
}

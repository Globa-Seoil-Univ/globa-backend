package org.y2k2.globa.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.InquiryService;
import org.y2k2.globa.util.JwtTokenProvider;

import java.net.URI;

@RestController
@RequestMapping("/inquiry")
@ResponseBody
@RequiredArgsConstructor
public class InquiryController {
    private final InquiryService inquiryService;
    private final JwtTokenProvider jwtTokenProvider;

    @GetMapping
    public ResponseEntity<?> getInquires(
            @RequestHeader(value = "Authorization") String accessToken,
            @RequestParam(required = false, value = "page", defaultValue = "1") int page,
            @RequestParam(required = false, value = "count", defaultValue = "10") int count,
            @RequestParam(required = false, value = "sort", defaultValue = "r") String sort
    ) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        SortDto sortDto = SortDto.valueOfString(sort);
        PaginationDto paginationDto = new PaginationDto(page, count, sortDto);
        ResponseInquiryDto dto = inquiryService.getInquiries(userId, paginationDto);
        return ResponseEntity.ok().body(dto);
    }

    @GetMapping(value = "/{inquiryId}")
    public ResponseEntity<?> getInquiry(@RequestHeader(value = "Authorization") String accessToken, @PathVariable(name = "inquiryId") long inquiryId) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        ResponseInquiryDetailDto dto = inquiryService.getInquiry(userId, inquiryId);
        return ResponseEntity.ok().body(dto);
    }

    @PostMapping
    public ResponseEntity<?> addInquiry(@RequestHeader(value = "Authorization") String accessToken, @Valid @RequestBody RequestInquiryDto dto) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        long userId = jwtTokenProvider.getUserIdByAccessToken(accessToken);

        long inquiryId = inquiryService.addInquiry(userId, dto);
        return ResponseEntity.created(URI.create("/" + inquiryId)).build();
    }
}

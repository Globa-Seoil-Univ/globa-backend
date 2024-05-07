package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.y2k2.globa.dto.DummyImageResponseDto;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.service.DummyImageService;
import org.y2k2.globa.util.JwtTokenProvider;

@RestController
@RequestMapping("/dummy")
@ResponseBody
@RequiredArgsConstructor
public class DummyImageController {
    private final DummyImageService dummyImageService;
    private final JwtTokenProvider jwtTokenProvider;

    @PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDummyImage(@RequestHeader(value = "Authorization") String accessToken, @RequestParam("image") MultipartFile file) {
        if (accessToken == null) {
            throw new BadRequestException("You must be requested to access token.");
        }
        jwtTokenProvider.getUserIdByAccessToken(accessToken);
        if (file.isEmpty()) throw new BadRequestException("You must request image field");

        DummyImageResponseDto responseDto = dummyImageService.addDummyImage(file);
        return ResponseEntity.ok().body(responseDto);
    }
}

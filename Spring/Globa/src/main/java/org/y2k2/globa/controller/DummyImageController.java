package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.entity.DummyImageEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.mapper.DummyImageMapper;
import org.y2k2.globa.service.DummyImageService;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class DummyImageController {
    private final String PRE_FIX = "/dummy";
    private final DummyImageService dummyImageService;

    @PostMapping(value = PRE_FIX + "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDummyImage(@RequestParam("image") MultipartFile file) {
        if (file.isEmpty()) throw new BadRequestException("You must request image field");

        DummyImageEntity entity = dummyImageService.addDummyImage(file);
        return ResponseEntity.ok().body(DummyImageMapper.INSTANCE.toResponseDto(entity));
    }
}
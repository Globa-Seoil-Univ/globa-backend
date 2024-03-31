package org.y2k2.globa.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.y2k2.globa.service.DummyImageService;

@RestController
@ResponseBody
@RequiredArgsConstructor
public class DummyImageController {
    private final String PRE_FIX = "/dummy";
    private final DummyImageService dummyImageService;

    @PostMapping(value = PRE_FIX + "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addDummyImage() {
        return ResponseEntity.ok().body("a");
    }
}

package org.y2k2.globa.dto;

import lombok.Getter;

@Getter
public class DummyImageResponseDto {
    private final Long imageId;
    private final String path;

    public DummyImageResponseDto(Long imageId, String path) {
        this.imageId = imageId;
        this.path = path;
    }
}

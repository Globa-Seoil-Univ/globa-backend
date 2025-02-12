package org.y2k2.globa.dto.common.file;

import lombok.Builder;

public record FileDto(
        String originalFileName,
        String storeFileName,
        String storePath,
        String extension,
        long size
) {
    @Builder
    public FileDto(String originalFileName, String storeFileName, String storePath, String extension, long size) {
        this.originalFileName = originalFileName;
        this.storeFileName = storeFileName;
        this.storePath = storePath;
        this.extension = extension;
        this.size = size;
    }
}

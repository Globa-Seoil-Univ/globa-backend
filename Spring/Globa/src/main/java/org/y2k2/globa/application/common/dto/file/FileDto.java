package org.y2k2.globa.application.common.dto.file;

import lombok.Builder;

/**
 * File 정보를 담는 DTO
 *
 * @param originalFileName 원본 파일명
 * @param storeFileName 저장 파일명
 * @param storePath 저장 경로
 * @param extension 확장자
 * @param size 파일 크기
 */
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

    public boolean isEmpty() {
        return originalFileName == null
                && storeFileName == null
                && storePath == null
                && extension == null;
    }
}

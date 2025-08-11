package org.y2k2.globa.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class FileUploadException extends RuntimeException {
    private final ErrorCode errorCode = ErrorCode.FAILED_FILE_UPLOAD;
    private final String filePath;
}

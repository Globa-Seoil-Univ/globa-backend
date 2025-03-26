package org.y2k2.globa.common.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;

@Aspect
@Component
@Slf4j
public class FileCleanupAspect {
    private final FileStore fileStore;

    public FileCleanupAspect(FileStore fileStore) {
        this.fileStore = fileStore;
    }

    @AfterThrowing(
            pointcut = "@annotation(org.y2k2.globa.common.annotation.FileCleanup)",
            throwing = "ex"
    )
    public void handleCleanup(Exception ex) throws Exception {
        if (ex instanceof FileUploadException fileUploadException) {
            log.error("File upload failed. Deleting file [path = {}]", fileUploadException.getFilePath());
            String filePath = fileUploadException.getFilePath();

            if (filePath != null) {
                fileStore.deleteFile(filePath);
            }

            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        throw ex;
    }
}

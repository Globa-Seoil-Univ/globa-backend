package org.y2k2.globa.util.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class FileStore {
    private Bucket bucket;

    private String getStoreFileName(String ext) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String getExtension(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    public FileDto storeFile(String directoryPath, MultipartFile file) {
        log.info("store file: [path = {}, name = {}]", directoryPath, file.getOriginalFilename());

        if (file.isEmpty()) {
            log.error("Failed to store empty file because file empty. [path = {}, name = {}]", directoryPath, file.getOriginalFilename());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null) {
            log.error("Failed to store file because can not found extension. [path = {}, name = {}]", directoryPath, file.getOriginalFilename());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        String ext = getExtension(originalFilename);
        String storeFileName = getStoreFileName(ext);
        String type = file.getContentType();
        String storeFilePath = directoryPath + storeFileName;
        long size = file.getSize();

        if (bucket.get(storeFileName) != null) {
            log.error("Failed to store file because file already exists. [path = {}, name = {}]", directoryPath, file.getOriginalFilename());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        try {
            bucket.create(storeFilePath, file.getBytes(), type);
        } catch (IOException e) {
            log.error("Failed to store file because can not read file. [path = {}, name = {}]", directoryPath, file.getOriginalFilename());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        } catch (Exception e) {
            log.error("Failed to store file because can not create file. [path = {}, name = {}, reason = {}]", directoryPath, file.getOriginalFilename(), e.getMessage());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        return FileDto.builder()
                .storeFileName(storeFileName)
                .originalFileName(originalFilename)
                .storePath(storeFilePath)
                .size(size)
                .extension(type)
                .build();
    }

    public void storeEmptyFile(String path) {
        byte[] content = new byte[0];

        try {
            bucket.create(path, content, "text/plain");
        } catch (Exception e) {
            log.error("Failed to store empty file because can not create file. [path = {}, reason = {}]", path, e.getMessage());
            throw new CustomException(ErrorCode.FAILED_FILE_UPLOAD);
        }

        log.info("store file: [path = {}]", path);
    }

    public void deleteFile(String storePath) {
        log.info("delete file: [name = {}]", storePath);

        try {
            Blob file = bucket.get(storePath);

            if (file == null) {
                log.error("Failed to delete file because file not found. [name = {}]", storePath);
                return;
            }

            file.delete();
        } catch (Exception e) {
            log.error("Failed to delete file because can not delete file. [path = {}, reason = {}]", storePath, e.getMessage());
        }
    }
}

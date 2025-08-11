package org.y2k2.globa.common.util.file;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Bucket;
import com.google.cloud.storage.Storage;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@AllArgsConstructor
public class FileStore {
    private Storage storage;
    private Bucket bucket;

    private String getStoreFileName(String ext) {
        String uuid = UUID.randomUUID().toString();
        return uuid + "." + ext;
    }

    private String getExtension(String originalFilename) {
        int pos = originalFilename.lastIndexOf(".");
        return originalFilename.substring(pos + 1);
    }

    /**
     * File을 Firebase Storage에서 가져옵니다.
     *
     * @param path 가져올 파일 경로
     * @return {@link FileDto} 가져온 파일 정보
     */
    public Optional<FileDto> getFile(String path) {
        log.info("get file: [path = {}]", path);

        Blob file = bucket.get(path);

        if (file == null || !file.exists()) {
            return Optional.empty();
        }

        return Optional.of(FileDto.builder()
                .storeFileName(file.getName())
                .originalFileName(file.getName())
                .storePath(file.getName())
                .size(file.getSize())
                .extension(file.getContentType())
                .build());
    }

    /**
     * File을 Firebase Storage에 저장합니다.
     *
     * @param directoryPath 저장할 경로
     * @param file 저장할 파일
     * @return {@link FileDto} 저장된 파일 정보
     */
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

    /**
     * 기존 File을 새로운 경로에 저장합니다. <br />
     * 단, 기존 파일은 삭제되지 않습니다.
     *
     * @param oldPath 저장된 파일 경로
     * @param newPath 저장할 파일 경로
     */
    public void moveFile(String oldPath, String newPath) {
        log.info("move file: [oldPath = {}, newPath = {}]", oldPath, newPath);

        Blob oldFile = bucket.get(oldPath);

        if (oldFile == null) {
            log.error("Failed to move file because file not found. [oldPath = {}, newPath = {}]", oldPath, newPath);
            throw new CustomException(ErrorCode.NOT_FOUND_RECORD_FIREBASE);
        }

        oldFile.copyTo(BlobId.of(bucket.getName(), newPath));
    }

    /**
     * File을 삭제합니다. <br />
     * File이 존재하지 않거나, 삭제하지 못했어도 에러를 발생시키지 않습니다.
     *
     * @param storePath 삭제할 파일 경로
     */
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

    /**
     * File들을 삭제합니다. <br />
     * File이 존재하지 않거나, 삭제하지 못했어도 에러를 발생시키지 않습니다.
     *
     * @param storePaths 삭제할 파일 경로들
     */
    public void deleteFiles(List<String> storePaths) {
        if (storePaths.isEmpty()) {
            return;
        }

        log.info("delete files: [names = {}]", storePaths);

        try {
            List<BlobId> blobIds = storePaths.stream()
                    .map(filePath -> BlobId.of(bucket.getName(), filePath))
                    .toList();

            storage.delete(blobIds);
        } catch (Exception e) {
            log.error("Failed to delete files because can not delete files. [path = {}, reason = {}]", storePaths, e.getMessage());
        }
    }
}

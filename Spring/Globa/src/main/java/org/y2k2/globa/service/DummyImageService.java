package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.y2k2.globa.dto.DummyImageResponseDto;
import org.y2k2.globa.entity.DummyImageEntity;
import org.y2k2.globa.exception.FileUploadException;
import org.y2k2.globa.mapper.DummyImageMapper;
import org.y2k2.globa.repository.DummyImageRepository;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class DummyImageService {
    @Autowired
    private Bucket bucket;
    private final String dummyPath = "dummies";

    private final DummyImageRepository dummyImageRepository;

    @Transactional
    public DummyImageResponseDto addDummyImage(MultipartFile file) {
        long current = new Date().getTime();
        long size = file.getSize();
        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String mimeType = file.getContentType();

        String path = dummyPath + "/" + current + "." + extension;

        try {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            bucket.create(path, file.getBytes());

            DummyImageEntity entity = DummyImageEntity.create(path, size, mimeType);
            DummyImageEntity savedEntity = dummyImageRepository.save(entity);

            return DummyImageMapper.INSTANCE.toResponseDto(savedEntity);
        } catch (Exception e) {
            if (bucket.get(path) != null) {
                bucket.get(path).delete();
            }

            throw new FileUploadException(e.getMessage());
        }
    }
}

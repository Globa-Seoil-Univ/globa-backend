package org.y2k2.globa.service;

import com.google.cloud.storage.Bucket;

import lombok.RequiredArgsConstructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import org.y2k2.globa.annotation.FileCleanup;
import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.dto.request.dummy.RequestDummyImageDto;
import org.y2k2.globa.dto.response.dummyimage.ResponseDummyImageDto;
import org.y2k2.globa.entity.DummyImageEntity;
import org.y2k2.globa.entity.RoleEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.exception.FileUploadException;
import org.y2k2.globa.mapper.DummyImageMapper;
import org.y2k2.globa.repository.DummyImageRepository;
import org.y2k2.globa.repository.RoleRepository;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.repository.UserRoleRepository;
import org.y2k2.globa.type.UserRole;
import org.y2k2.globa.util.file.FileStore;
import org.y2k2.globa.util.jwt.JWTProvider;

import java.util.Date;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DummyImageService {
    private final FileStore fileStore;

    private final UserRoleService userRoleService;

    private final DummyImageRepository dummyImageRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    @FileCleanup
    public ResponseDummyImageDto addDummyImage(RequestDummyImageDto dto, UserEntity user) {
        Optional<UserRoleEntity> optionalUserRole = userRoleRepository.findByUser(user);

        if (optionalUserRole.isEmpty()) {
            userRoleService.createUserRoleAndThrowException(user);
        } else {
            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
        }

        FileDto fileDto = fileStore.storeFile("notices/images/", dto.image());

        try {
            DummyImageEntity dummyImage = DummyImageMapper.INSTANCE.toEntity(fileDto);
            DummyImageEntity createdDummyImage = dummyImageRepository.save(dummyImage);
            return DummyImageMapper.INSTANCE.toResponseDto(createdDummyImage);
        } catch (Exception e) {
            throw new FileUploadException(fileDto.storePath());
        }
    }
}

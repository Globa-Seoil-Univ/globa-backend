package org.y2k2.globa.application.dummyimage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.application.dummyimage.mapper.DummyImageMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.usecase.CreateUserRoleUseCase;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;

@Service
@RequiredArgsConstructor
public class CreateDummyImageService {
    private final VerifyUserWritableUseCase verifyUserWritableUseCase;

    private final DummyImageRepository dummyImageRepository;

    private final FileStore fileStore;

    @FileCleanup
    public ResponseDummyImageDto create(RequestDummyImageDto dto, Long userId) {
        verifyUserWritableUseCase.execute(userId);

        FileDto file = fileStore.storeFile("notices/images/", dto.image());

        try {
            DummyImageEntity dummyImage = DummyImageMapper.INSTANCE.toEntity(file);
            DummyImageEntity createdDummyImage = dummyImageRepository.save(dummyImage);
            return DummyImageMapper.INSTANCE.toResponseDto(createdDummyImage);
        } catch (Exception e) {
            throw new FileUploadException(file.storePath());
        }
    }
}

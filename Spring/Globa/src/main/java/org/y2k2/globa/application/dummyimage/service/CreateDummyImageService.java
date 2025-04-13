package org.y2k2.globa.application.dummyimage.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.dummyimage.dto.request.RequestDummyImageDto;
import org.y2k2.globa.application.dummyimage.dto.response.ResponseDummyImageDto;
import org.y2k2.globa.application.dummyimage.mapper.DummyImageMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.command.CreateUserRoleCommand;
import org.y2k2.globa.application.userrole.usecase.CreateUserRoleUseCase;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

@Service
@RequiredArgsConstructor
public class CreateDummyImageService {
    private final FindUserUseCase findUserUseCase;
    private final CreateUserRoleUseCase createUserRoleUseCase;

    private final UserRoleRepository userRoleRepository;
    private final DummyImageRepository dummyImageRepository;

    private final FileStore fileStore;

    @FileCleanup
    public ResponseDummyImageDto create(RequestDummyImageDto dto, Long userId) {
        UserRoleEntity userRole = userRoleRepository.getUserRole(userId)
                .orElseThrow(() -> {
                    UserEntity user = findUserUseCase.execute(userId);
                    createUserRoleUseCase.execute(CreateUserRoleCommand.of(user, UserRole.USER));
                    return new CustomException(ErrorCode.NOT_PERMISSION);
                });

        UserRole roleName = userRole.getRole().getName();
        if (!(roleName.equals(UserRole.ADMIN) || roleName.equals(UserRole.EDITOR))) {
            throw new CustomException(ErrorCode.NOT_PERMISSION);
        }

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

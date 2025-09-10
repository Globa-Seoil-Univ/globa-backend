package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestNameDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class UpdateUserNameService {
    private final FindUserUseCase findUserUseCase;
    private final FindFolderRoleUseCase findFolderRoleUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateFolderNameUseCase updateFolderNameUseCase;
    private final CreateDefaultFolderUseCase createDefaultFolderUseCase;

    private final FolderRepository folderRepository;

    @Transactional
    @CacheEvict(
            value = "user",
            key = "#userId",
            condition = "#userId != null"
    )
    public void update(RequestNameDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        FolderRoleEntity folderRole = findFolderRoleUseCase.execute(
                FolderRoleCommand.of(FolderRole.OWNER)
        ).orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER_ROLE));

        updateUserUseCase.execute(
                UpdateUserCommand.builder()
                        .user(user)
                        .name(dto.name())
                        .uploadNofi(user.getUploadNofi())
                        .shareNofi(user.getShareNofi())
                        .eventNofi(user.getEventNofi())
                        .profileImage(
                                FileDto.builder()
                                        .storePath(user.getProfilePath())
                                        .size(user.getProfileSize())
                                        .extension(user.getProfileType())
                                        .build()
                        )
                        .build()
        );

        FolderEntity folder = folderRepository.getDefaultFolder(userId)
                .orElseGet(() -> createDefaultFolderUseCase.execute(
                        CreateDefaultFolderCommand.of(folderRole, user)
                ));

        updateFolderNameUseCase.execute(
                UpdateFolderNameCommand.of(folder, dto.name())
        );
    }
}

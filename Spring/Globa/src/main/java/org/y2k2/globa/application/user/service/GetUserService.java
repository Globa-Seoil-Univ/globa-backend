package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.CreateFolderRoleUseCase;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.user.dto.response.ResponseUserDto;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class GetUserService {
    private final FindUserUseCase findUserUseCase;
    private final FindFolderRoleUseCase findFolderRoleUseCase;
    private final CreateFolderRoleUseCase createFolderRoleUseCase;
    private final CreateDefaultFolderUseCase createDefaultFolderUseCase;

    private final FolderRepository folderRepository;

    public ResponseUserDto getUser(Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        FolderRoleEntity folderRole = findFolderRoleUseCase.execute(
                FolderRoleCommand.of(FolderRole.OWNER)
        ).orElseGet(
                () -> createFolderRoleUseCase.execute(FolderRoleCommand.of(FolderRole.OWNER))
        );

        FolderEntity folder = folderRepository.getDefaultFolder(userId)
                .orElseGet(
                        () -> createDefaultFolderUseCase.execute(
                                new CreateDefaultFolderCommand(folderRole, user)
                        )
                );

        return UserMapper.INSTANCE.toResponseUserDto(user, folder.getFolderId());
    }
}

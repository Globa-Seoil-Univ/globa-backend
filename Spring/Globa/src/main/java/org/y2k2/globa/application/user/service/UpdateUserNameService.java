package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.command.UpdateFolderNameCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folder.usecase.UpdateFolderNameUseCase;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestNameDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class UpdateUserNameService {
    private final FindUserUseCase findUserUseCase;
    private final GetFolderRoleUseCase getFolderRoleUseCase;
    private final UpdateUserUseCase updateUserUseCase;
    private final UpdateFolderNameUseCase updateFolderNameUseCase;
    private final CreateDefaultFolderUseCase createDefaultFolderUseCase;

    private final FolderRepository folderRepository;

    @Transactional
    public void update(RequestNameDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        FolderRoleEntity folderRole = getFolderRoleUseCase.execute(
                GetFolderRoleCommand.of(FolderRole.OWNER)
        );

        updateUserUseCase.execute(
                UpdateUserCommand.builder()
                        .user(user)
                        .name(dto.name())
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

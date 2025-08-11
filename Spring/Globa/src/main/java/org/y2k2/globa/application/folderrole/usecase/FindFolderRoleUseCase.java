package org.y2k2.globa.application.folderrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.folderrole.repository.FolderRoleRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class FindFolderRoleUseCase implements UseCase<FolderRoleCommand, Optional<FolderRoleEntity>> {
    private final FolderRoleRepository folderRoleRepository;

    @Override
    @Cacheable(
            value = "folderRole",
            key = "#command.folderRole.name()",
            condition = "#command.folderRole.name() != null",
            unless = "#result == null || #result.getRoleId() == null"
    )
    public Optional<FolderRoleEntity> execute(FolderRoleCommand command) {
        return folderRoleRepository.getRole(command.folderRole());
    }
}

package org.y2k2.globa.application.folderrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.folderrole.repository.FolderRoleRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;

@Component
@RequiredArgsConstructor
public class GetFolderRoleUseCase implements UseCase<GetFolderRoleCommand, FolderRoleEntity> {
    private final FolderRoleRepository folderRoleRepository;

    @Override
    public FolderRoleEntity execute(GetFolderRoleCommand command) {
        FolderRoleEntity folderRole;

        if (command.folderRole().equals(FolderRole.WRITER)) {
            folderRole = folderRoleRepository.getRole(FolderRole.WRITER.getRoleName())
                    .orElseGet(() -> {
                        FolderRoleEntity folderRoleEntity = new FolderRoleEntity();
                        folderRoleEntity.setRoleName(FolderRole.WRITER.getRoleName());
                        return folderRoleRepository.save(folderRoleEntity);
                    });
        } else {
            folderRole = folderRoleRepository.getRole(FolderRole.READER.getRoleName())
                    .orElseGet(() -> {
                        FolderRoleEntity folderRoleEntity = new FolderRoleEntity();
                        folderRoleEntity.setRoleName(FolderRole.READER.getRoleName());
                        return folderRoleRepository.save(folderRoleEntity);
                    });
        }

        return folderRole;
    }
}

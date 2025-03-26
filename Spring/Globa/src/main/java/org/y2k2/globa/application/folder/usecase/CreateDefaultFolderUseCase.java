package org.y2k2.globa.application.folder.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folder.SaveDefaultFolderCommand;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.type.InvitationStatus;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.insfrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.insfrastructure.persistence.folder.repository.FolderJpaRepository;
import org.y2k2.globa.insfrastructure.persistence.folderrole.repository.FolderRoleJpaRepository;
import org.y2k2.globa.insfrastructure.persistence.foldershare.repository.FolderShareJpaRepository;

@RequiredArgsConstructor
@Component
public class CreateDefaultFolderUseCase implements VoidUseCase<SaveDefaultFolderCommand> {
    private final FolderJpaRepository folderJpaRepository;
    private final FolderRoleJpaRepository folderRoleJpaRepository;
    private final FolderShareJpaRepository folderShareJpaRepository;

    @Override
    public void execute(SaveDefaultFolderCommand command) {
        FolderRoleEntity role = folderRoleJpaRepository.findByRoleName(FolderRole.OWNER.getRoleName())
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        FolderEntity folder = FolderMapper.INSTANCE.toEntity(command.user(), command.user().getName() + "님의 폴더");
        FolderEntity createdFolder = folderJpaRepository.save(folder);

        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                createdFolder,
                InvitationStatus.ACCEPT,
                role,
                command.user(),
                command.user()
        );

        folderShareJpaRepository.save(folderShare);
    }
}

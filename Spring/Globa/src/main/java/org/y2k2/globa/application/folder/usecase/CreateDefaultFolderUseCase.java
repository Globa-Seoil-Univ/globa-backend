package org.y2k2.globa.application.folder.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.folderrole.repository.FolderRoleRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

@RequiredArgsConstructor
@Component
public class CreateDefaultFolderUseCase implements VoidUseCase<CreateDefaultFolderCommand> {
    private final FolderRepository folderRepository;
    private final FolderRoleRepository folderRoleRepository;
    private final FolderShareRepository folderShareRepository;

    @Override
    public void execute(CreateDefaultFolderCommand command) {
        FolderRoleEntity role = folderRoleRepository.getRole(FolderRole.OWNER.getRoleName())
                .orElseThrow(() -> new CustomException(ErrorCode.INTERNAL_SERVER_ERROR));

        FolderEntity folder = FolderMapper.INSTANCE.toEntity(command.user(), command.user().getName() + "님의 폴더");
        FolderEntity createdFolder = folderRepository.save(folder);

        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                createdFolder,
                InvitationStatus.ACCEPT,
                role,
                command.user(),
                command.user()
        );

        folderShareRepository.save(folderShare);
    }
}

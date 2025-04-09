package org.y2k2.globa.application.folder.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folderrole.command.GetFolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.GetFolderRoleUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFoldersService {
    private final FindUserUseCase findUserUseCase;
    private final GetFolderRoleUseCase getFolderRoleUseCase;
    private final CreateDefaultFolderUseCase createDefaultFolderUseCase;

    private final FolderRepository folderRepository;
    private final FolderShareRepository folderShareRepository;

    public ResponseFolderDto getFolders(int page, int count, Long userId) {
        Pageable pageable;
        List<FolderEntity> folders = new ArrayList<>();

        if (page == 1) {
            pageable = PageRequest.of(0, count - 1);

            UserEntity user = findUserUseCase.execute(userId);
            FolderRoleEntity folderRole = getFolderRoleUseCase.execute(
                    GetFolderRoleCommand.of(FolderRole.OWNER)
            );

            FolderEntity defaultFolder = folderRepository.getDefaultFolder(userId)
                    .orElseGet(() -> createDefaultFolderUseCase.execute(CreateDefaultFolderCommand.of(folderRole, user)));

            folders.add(defaultFolder);
        } else {
            pageable = PageRequest.of(page - 1, count);
        }

        Page<FolderShareEntity> folderShareEntities = folderShareRepository.getInvitationsForFolderExcludingDefault(
                userId,
                pageable
        );

        folders.addAll(folderShareEntities.stream()
                .map(FolderShareEntity::getFolder)
                .toList()
        );

        List<ResponseFolderDto.FolderDto> dtos = folders.stream()
                .map(FolderMapper.INSTANCE::toResponseInFolderDto)
                .toList();

        return new ResponseFolderDto(dtos, folderShareEntities.getTotalElements());
    }
}

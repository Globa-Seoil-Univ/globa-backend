package org.y2k2.globa.application.foldershare.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetFolderSharesService {
    private final VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;

    private final FolderShareRepository folderShareRepository;

    public ResponseFolderShareUserDto get(Long folderId, int page, int count, Long ownerId) {
        verifyFolderOwnerUseCase.execute(VerifyFolderCommand.of(ownerId, folderId));

        Pageable pageable = PageRequest.of(page - 1, count);
        Page<FolderShareEntity> folderShares = folderShareRepository.getShareInvitations(folderId, pageable);

        Long total = folderShares.getTotalElements();
        List<ResponseFolderShareUserDto.FolderShareUserDto> response = folderShares.stream()
                .map(FolderShareMapper.INSTANCE::toShareUserDto)
                .toList();

        return new ResponseFolderShareUserDto(
                response,
                total
        );
    }
}

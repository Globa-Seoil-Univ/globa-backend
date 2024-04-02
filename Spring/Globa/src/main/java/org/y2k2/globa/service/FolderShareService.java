package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.FolderShareUserDto;
import org.y2k2.globa.dto.FolderShareUserResponseDto;
import org.y2k2.globa.dto.Role;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderRoleEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.BadRequestException;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.mapper.FolderShareMapper;
import org.y2k2.globa.repository.FolderRepository;
import org.y2k2.globa.repository.FolderRoleRepository;
import org.y2k2.globa.repository.FolderShareRepository;
import org.y2k2.globa.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FolderShareService {
    private final FolderShareRepository folderShareRepository;
    private final FolderRepository folderRepository;
    private final FolderRoleRepository folderRoleRepository;
    private final UserRepository userRepository;

    public FolderShareUserResponseDto getShares(Long folderId, Long userId, int page, int count) {
        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);

        if (folderEntity == null) throw new NotFoundException("Not found folder");
        if (!folderEntity.getUser().getUserId().equals(userId)) throw new ForbiddenException("You are not owned this folder");
        
        Pageable pageable = PageRequest.of(page - 1, count);
        Page<FolderShareEntity> folderShareEntityPage = folderShareRepository.findByFolderIdOrderByCreatedTimeAsc(pageable, folderId);

        List<FolderShareEntity> shareEntities = folderShareEntityPage.getContent();
        Long total = folderShareEntityPage.getTotalElements();
        List<FolderShareUserDto> folderShareUserDtos = shareEntities.stream()
                .map(FolderShareMapper.INSTANCE::toShareUserDto)
                .collect(Collectors.toList());

        return new FolderShareUserResponseDto(folderShareUserDtos, total);
    }

    @Transactional
    public void inviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity ownerEntity = userRepository.findByUserId(ownerId);
        UserEntity targetEntity = userRepository.findByUserId(targetId);
        checkValidation(folderId, ownerId, targetId, targetEntity);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUser(targetEntity);
        if (folderShareEntity != null) throw new BadRequestException("This user has already been shared or sent a share request");

        FolderRoleEntity folderRoleEntity = convertRole(role);
        FolderShareEntity entity = FolderShareEntity.create(folderId, ownerEntity, targetEntity, folderRoleEntity);
        folderShareRepository.save(entity);
    }

    @Transactional
    public void editInviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);
        checkValidation(folderId, ownerId, targetId, targetEntity);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByFolderIdAndTargetUser(folderId, targetEntity);
        if (folderShareEntity == null) throw new NotFoundException("Not found folder share");

        FolderRoleEntity folderRoleEntity = convertRole(role);
        folderShareEntity.setRoleId(folderRoleEntity);
        folderShareRepository.save(folderShareEntity);
    }

    @Transactional
    public void deleteInviteShare(Long folderId, Long ownerId, Long targetId) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);
        checkValidation(folderId, ownerId, targetId, targetEntity);

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByFolderIdAndTargetUser(folderId, targetEntity);
        if (folderShareEntity == null) throw new NotFoundException("Not found folder share");

        folderShareRepository.delete(folderShareEntity);
    }

    private void checkValidation(Long folderId, Long ownerId, Long targetId, UserEntity targetEntity) {
        if (ownerId.equals(targetId)) throw new BadRequestException("You can't invite yourself");

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new NotFoundException("Not found folder");
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new ForbiddenException("You aren't owned this folder");

        if (targetEntity == null) throw new BadRequestException("Not found target user");
    }

    private FolderRoleEntity convertRole(Role role) {
        FolderRoleEntity folderRoleEntity;

        if (role.equals(Role.W)) {
            folderRoleEntity = folderRoleRepository.findByRoleName("편집자");
        } else {
            folderRoleEntity = folderRoleRepository.findByRoleName("뷰어");
        }

        return folderRoleEntity;
    }
}

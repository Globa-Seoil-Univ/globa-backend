package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.FolderShareUserDto;
import org.y2k2.globa.dto.FolderShareUserResponseDto;
import org.y2k2.globa.dto.InvitationStatus;
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
        Page<FolderShareEntity> folderShareEntityPage = folderShareRepository.findByFolderOrderByCreatedTimeAsc(pageable, folderEntity);

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
        if (ownerId.equals(targetId)) throw new BadRequestException("You can't invite yourself");
        if (targetEntity == null) throw new BadRequestException("Not found target user");

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new NotFoundException("Not found folder");
        if (!folderEntity.getUser().getUserId().equals(ownerId)) throw new ForbiddenException("You aren't owned this folder");

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByTargetUser(targetEntity);
        if (folderShareEntity != null) throw new BadRequestException("This user has already been shared or sent a share request");

        FolderRoleEntity folderRoleEntity = convertRole(role);
        FolderShareEntity entity = FolderShareEntity.create(folderEntity, ownerEntity, targetEntity, folderRoleEntity);
        folderShareRepository.save(entity);
    }

    @Transactional
    public void editInviteShare(Long folderId, Long ownerId, Long targetId, Role role) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);
        if (targetEntity == null) throw new BadRequestException("Not found target user");

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new NotFoundException("Not found folder");

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new NotFoundException("Not found folder share");

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        FolderRoleEntity folderRoleEntity = convertRole(role);
        folderShareEntity.setRoleId(folderRoleEntity);
        folderShareRepository.save(folderShareEntity);
    }

    @Transactional
    public void deleteInviteShare(Long folderId, Long ownerId, Long targetId) {
        UserEntity targetEntity = userRepository.findByUserId(targetId);
        if (targetEntity == null) throw new BadRequestException("Not found target user");

        FolderEntity folderEntity = folderRepository.findFirstByFolderId(folderId);
        if (folderEntity == null) throw new NotFoundException("Not found folder");

        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByFolderAndTargetUser(folderEntity, targetEntity);
        if (folderShareEntity == null) throw new NotFoundException("Not found folder share");

        checkValidation(folderShareEntity.getFolder(), ownerId, targetId, targetEntity);

        folderShareRepository.delete(folderShareEntity);
    }

    @Transactional
    public void acceptShare(Long folderId, Long shareId, Long targetId) {
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByShareId(shareId);
        checkValidation(folderShareEntity, folderId, targetId);

        folderShareEntity.setInvitationStatus(String.valueOf(InvitationStatus.ACCEPT));
        folderShareRepository.save(folderShareEntity);
    }
    @Transactional
    public void refuseShare(Long folderId, Long shareId, Long targetId) {
        FolderShareEntity folderShareEntity = folderShareRepository.findFirstByShareId(shareId);
        checkValidation(folderShareEntity, folderId, targetId);
        folderShareRepository.delete(folderShareEntity);
    }

    private void checkValidation(FolderShareEntity folderShareEntity, Long folderId, Long targetId) {
        if (folderShareEntity == null) throw new NotFoundException("Not found folderShare");
        if (!folderShareEntity.getFolder().getFolderId().equals(folderId)) throw new ForbiddenException("Can't modify invitations in other folders");
        if (folderShareEntity.getInvitationStatus().equals(String.valueOf(InvitationStatus.ACCEPT))) throw new BadRequestException("Already accept invitation");

        UserEntity targetEntity = folderShareEntity.getTargetUser();
        if (targetEntity == null) throw new BadRequestException("Not found target user");
        if (!targetEntity.getUserId().equals(targetId)) throw new ForbiddenException("You can't change someone's invitation");
    }

    private void checkValidation(FolderEntity folderEntity, Long ownerId, Long targetId, UserEntity targetEntity) {
        if (ownerId.equals(targetId)) throw new BadRequestException("You can't invite yourself");
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

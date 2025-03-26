package org.y2k2.globa.insfrastructure.persistence.foldershare.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.common.type.InvitationStatus;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.domain.foldershare.FolderShareRepository;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class FolderShareRepositoryImpl implements FolderShareRepository {
    private final FolderShareJpaRepository folderShareJpaRepository;

    @Override
    public FolderShareEntity save(FolderShareEntity entity) {
        return folderShareJpaRepository.save(entity);
    }

    @Override
    public Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatus(UserEntity user, Long folderId, InvitationStatus status) {
        return folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, status);
    }

    @Override
    public Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(UserEntity user, Long folderId, InvitationStatus status, String roleName) {
        return folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(user, folderId, status, roleName);
    }

    @Override
    public Boolean existsByFolderAndTargetUser(FolderEntity folder, UserEntity user) {
        return folderShareJpaRepository.existsByFolderAndTargetUser(folder, user);
    }

    @Override
    public Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(FolderEntity folder, Pageable pageable) {
        return folderShareJpaRepository.findByFolderOrderByCreatedTimeAsc(folder, pageable);
    }

    @Override
    public Page<FolderShareEntity> findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(UserEntity ownerUser, UserEntity targetUser, InvitationStatus status, Pageable pageable) {
        return folderShareJpaRepository.findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(ownerUser, targetUser, status, pageable);
    }

    @Override
    public List<FolderShareEntity> findAllByFolderFolderId(Long folderId) {
        return folderShareJpaRepository.findAllByFolderFolderId(folderId);
    }

    @Override
    public List<FolderShareEntity> findAllByFolderFolderIdAndTargetUser_UserIdNot(Long folderId, Long excludeId) {
        return folderShareJpaRepository.findAllByFolderFolderIdAndTargetUser_UserIdNot(folderId, excludeId);
    }

    @Override
    public Optional<FolderShareEntity> findByShareId(Long folderId) {
        return folderShareJpaRepository.findByShareId(folderId);
    }

    @Override
    public Optional<FolderShareEntity> findByFolderAndTargetUser(FolderEntity folder, UserEntity user) {
        return folderShareJpaRepository.findByFolderAndTargetUser(folder, user);
    }

    @Override
    public Optional<FolderShareEntity> findByFolderAndTargetUserJoinRole(FolderEntity folder, UserEntity user) {
        return folderShareJpaRepository.findByFolderAndTargetUserJoinRole(folder, user);
    }
}

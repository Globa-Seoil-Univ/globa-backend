package org.y2k2.globa.infrastructure.persistence.foldershare.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

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
    public List<FolderShareEntity> saveAll(List<FolderShareEntity> entities) {
        return folderShareJpaRepository.saveAll(entities);
    }

    @Override
    public void delete(FolderShareEntity entity) {
        folderShareJpaRepository.delete(entity);
    }

    @Override
    public Boolean isAccessible(Long userId, Long folderId) {
        return folderShareJpaRepository.existsByTargetUser_UserIdAndFolderFolderIdAndInvitationStatus(userId, folderId, InvitationStatus.ACCEPT);
    }

    @Override
    public Boolean isOwner(Long userId, Long folderId) {
        return folderShareJpaRepository.existsByTargetUser_UserIdAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
                userId,
                folderId,
                InvitationStatus.ACCEPT,
                FolderRole.OWNER.getRoleName()
        );
    }

    @Override
    public Boolean isInvited(Long userId, Long folderId) {
        return folderShareJpaRepository.existsByTargetUser_UserIdAndFolder_FolderId(userId, folderId);
    }

    @Override
    public Boolean isWritable(Long userId, Long folderId) {
        return folderShareJpaRepository.existsByTargetUser_UserIdAndFolder_FolderIdAndRoleIn(
                userId,
                folderId,
                InvitationStatus.ACCEPT,
                List.of(FolderRole.WRITER.getRoleName(), FolderRole.OWNER.getRoleName())
        );
    }

    @Override
    public Page<FolderShareEntity> getShareInvitations(Long folderId, Pageable pageable) {
        return folderShareJpaRepository.findByFolder_FolderIdOrderByCreatedTimeAsc(folderId, pageable);
    }

    @Override
    public Page<FolderShareEntity> getInvitationsForFolderExcludingDefault(Long userId, Pageable pageable) {
        return folderShareJpaRepository.findByInvitationsForFolderExcludingDefault(userId, InvitationStatus.ACCEPT, pageable);

    }

    @Override
    public List<FolderShareEntity> getAllShareInvitations(Long folderId) {
        return folderShareJpaRepository.findAllByFolderFolderId(folderId);
    }

    @Override
    public List<FolderShareEntity> getAllShareInvitationsWithoutMe(Long folderId, Long excludeId) {
        return folderShareJpaRepository.findAllByFolderFolderIdAndTargetUser_UserIdNot(folderId, excludeId);
    }

    @Override
    public Optional<FolderShareEntity> getShareInvitation(Long folderId, Long userId) {
        return folderShareJpaRepository.findByFolder_FolderIdAndTargetUser_UserId(folderId, userId);
    }

    @Override
    public Optional<FolderShareEntity> getShareInvitationWithFolder(Long folderId, Long userId) {
        return folderShareJpaRepository.findByFolderAndTargetUserJoinFolder(folderId, userId);
    }

    @Override
    public Optional<FolderShareEntity> getShareInvitationWithRole(FolderEntity folder, UserEntity user) {
        return folderShareJpaRepository.findByFolderAndTargetUserJoinRole(folder, user);
    }
}

package org.y2k2.globa.domain.foldershare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FolderShareRepository {
    FolderShareEntity save(FolderShareEntity entity);
    List<FolderShareEntity> saveAll(List<FolderShareEntity> entities);
    void delete(FolderShareEntity entity);

    Boolean isAccessible(UserEntity user, Long folderId);
    Boolean isOwner(
            Long userId,
            Long folderId
    );
    Boolean isInvited(FolderEntity folder, UserEntity user);

    Page<FolderShareEntity> getShareInvitations(FolderEntity folder, Pageable pageable);
    Page<FolderShareEntity> getInvitationsForFolderExcludingDefault(
            Long userId,
            Pageable pageable
    );

    List<FolderShareEntity> getAllShareInvitations(Long folderId);
    List<FolderShareEntity> getAllShareInvitationsWithoutMe(Long folderId, Long excludeId);

    Optional<FolderShareEntity> getShareInvitation(FolderEntity folder, UserEntity user);
    Optional<FolderShareEntity> getShareInvitationWithRole(FolderEntity folder, UserEntity user);
}

package org.y2k2.globa.domain.foldershare;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.common.type.InvitationStatus;
import org.y2k2.globa.insfrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.insfrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FolderShareRepository {
    FolderShareEntity save(FolderShareEntity entity);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatus(UserEntity user, Long folderId, InvitationStatus status);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
            UserEntity user,
            Long folderId,
            InvitationStatus status,
            String roleName
    );
    Boolean existsByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(FolderEntity folder, Pageable pageable);
    Page<FolderShareEntity> findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(
            UserEntity ownerUser,
            UserEntity targetUser,
            InvitationStatus status,
            Pageable pageable
    );

    List<FolderShareEntity> findAllByFolderFolderId(Long folderId);

    List<FolderShareEntity> findAllByFolderFolderIdAndTargetUser_UserIdNot(Long folderId, Long excludeId);

    Optional<FolderShareEntity> findByShareId(Long folderId);

    Optional<FolderShareEntity> findByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    Optional<FolderShareEntity> findByFolderAndTargetUserJoinRole(FolderEntity folder, UserEntity user);
}

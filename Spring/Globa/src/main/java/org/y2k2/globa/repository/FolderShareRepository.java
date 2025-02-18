package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.type.InvitationStatus;

import java.util.List;
import java.util.Optional;

public interface FolderShareRepository extends JpaRepository<FolderShareEntity, Long> {
    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(FolderEntity folder, Pageable pageable);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatus(UserEntity user, Long folderId, InvitationStatus status);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
            UserEntity user,
            Long folderId,
            InvitationStatus status,
            String roleName
    );
    Optional<FolderShareEntity> findFirstByShareId(Long folderId);

    @EntityGraph(value = "FolderShare.getFolderShareAndFolder", attributePaths = {
            "folder"
    }, type = EntityGraph.EntityGraphType.FETCH)
    @Query(
            "SELECT fs FROM FolderShareEntity fs " +
                    "WHERE (" +
                        "fs.ownerUser = :ownerUser " +
                        "OR fs.targetUser = :targetUser" +
                    ") " +
                    "AND fs.invitationStatus = :status " +
                    "AND fs.folder.folderId != (" +
                        "SELECT MIN(f.folderId) FROM FolderEntity f " +
                        "WHERE f.user = :ownerUser" +
                    ") " +
                    "ORDER BY fs.shareId DESC"
    )
    Page<FolderShareEntity> findAllByOwnerUserOrTargetUserAndInvitationStatusOrderByShareIdDesc(
            UserEntity ownerUser,
            UserEntity targetUser,
            InvitationStatus status,
            Pageable pageable
    );
  
    Optional<FolderShareEntity> findByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    Boolean existsByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    @EntityGraph(value = "FolderShare.getFolderShareAndUser", attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.LOAD)
    List<FolderShareEntity> findAllByFolderFolderId(Long folderId);

    @EntityGraph(value = "FolderShare.getFolderShareAndUser", attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.LOAD)
    List<FolderShareEntity> findAllByFolderFolderIdAndTargetUser_UserIdNot(Long folderId, Long excludeId);
}

package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.FolderShareEntity;
import org.y2k2.globa.entity.RoleEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.type.InvitationStatus;
import org.y2k2.globa.type.Role;

import java.util.List;
import java.util.Optional;

public interface FolderShareRepository extends JpaRepository<FolderShareEntity, Long> {
    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(Pageable pageable, FolderEntity folder);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatus(UserEntity user, Long folderId, InvitationStatus status);
    Boolean existsByTargetUserAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
            UserEntity user,
            Long folderId,
            InvitationStatus status,
            String roleName
    );
    FolderShareEntity findFirstByShareId(Long folderId);

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
  
    FolderShareEntity findByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    List<FolderShareEntity> findAllByFolderAndTargetUser_CodeIn(FolderEntity folder, List<String> codes);

    @EntityGraph(value = "FolderShare.getFolderShareAndUser", attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.LOAD)
    List<FolderShareEntity> findAllByFolderFolderId(long folderId);
}

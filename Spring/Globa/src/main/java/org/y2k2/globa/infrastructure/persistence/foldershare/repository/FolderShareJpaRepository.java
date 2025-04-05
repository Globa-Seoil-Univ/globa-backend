package org.y2k2.globa.infrastructure.persistence.foldershare.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

public interface FolderShareJpaRepository extends JpaRepository<FolderShareEntity, Long> {
    Boolean existsByTargetUser_UserIdAndFolderFolderIdAndInvitationStatus(Long userId, Long folderId, InvitationStatus status);

    Boolean existsByTargetUser_UserIdAndFolderFolderIdAndInvitationStatusAndRole_RoleName(
            Long userId,
            Long folderId,
            InvitationStatus status,
            String roleName
    );

    Boolean existsByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    Page<FolderShareEntity> findByFolderOrderByCreatedTimeAsc(FolderEntity folder, Pageable pageable);

    @Query(
            "SELECT fs FROM FolderShareEntity fs " +
                    "JOIN FETCH fs.folder f " +
                    "WHERE f.folderId <> (" +
                        "SELECT MIN(f2.folderId) FROM FolderEntity f2 " +
                            "WHERE f2.user.userId = :userId"+
                    ") " +
                    "AND fs.ownerUser.userId = :userId OR fs.targetUser.userId = :userId " +
                    "AND fs.invitationStatus = :status"
    )
    Page<FolderShareEntity> findByInvitationsForFolderExcludingDefault(Long userId, InvitationStatus status, Pageable pageable);

    @EntityGraph(attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.FETCH)
    List<FolderShareEntity> findAllByFolderFolderId(Long folderId);

    @EntityGraph(attributePaths = {
            "targetUser"
    }, type = EntityGraph.EntityGraphType.FETCH)
    List<FolderShareEntity> findAllByFolderFolderIdAndTargetUser_UserIdNot(Long folderId, Long excludeId);
  
    Optional<FolderShareEntity> findByFolderAndTargetUser(FolderEntity folder, UserEntity user);

    @Query(
            "SELECT fs FROM FolderShareEntity fs " +
                    "JOIN FETCH fs.role " +
                    "WHERE fs.folder = :folder " +
                    "AND fs.targetUser = :user"
    )
    Optional<FolderShareEntity> findByFolderAndTargetUserJoinRole(FolderEntity folder, UserEntity user);
}

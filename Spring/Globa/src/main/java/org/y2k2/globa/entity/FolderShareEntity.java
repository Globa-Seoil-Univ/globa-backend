package org.y2k2.globa.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;
import org.y2k2.globa.dto.InvitationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "folder_share")
public class FolderShareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id", columnDefinition = "INT UNSIGNED")
    private long shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "folder_id", referencedColumnName = "folder_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner_id", referencedColumnName = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity ownerUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "target_id", referencedColumnName = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "role_id", referencedColumnName = "role_id")
    private FolderRoleEntity roleId;

    @Column(name = "invitation_status")
    @Check(constraints = "invitation_status IN ('PENDING', 'ACCEPT')")
    private String invitationStatus;

    @CreationTimestamp
    @Column(name = "invitation_time")
    private LocalDateTime invitationTime;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    public void prePersist() {
        if (this.invitationStatus == null) this.setInvitationStatus(String.valueOf(InvitationStatus.PENDING));
    }

    public static FolderShareEntity create(FolderEntity folder, UserEntity ownerUser, UserEntity targetUser, FolderRoleEntity folderRole) {
        FolderShareEntity entity = new FolderShareEntity();

        entity.setFolder(folder);
        entity.setOwnerUser(ownerUser);
        entity.setTargetUser(targetUser);
        entity.setRoleId(folderRole);

        return entity;
    }
}

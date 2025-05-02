package org.y2k2.globa.infrastructure.persistence.foldershare.entity;

import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Table;

import lombok.Getter;
import lombok.Setter;

import org.hibernate.annotations.*;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "folder_share")
public class FolderShareEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "share_id", columnDefinition = "INT UNSIGNED")
    private Long shareId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "folder_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "owner_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity ownerUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "target_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity targetUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "role_id")
    private FolderRoleEntity role;

    @Enumerated(EnumType.STRING)
    @Column(name = "invitation_status", length = 7, columnDefinition = "ENUM('PENDING', 'ACCEPT') DEFAULT 'PENDING'")
    private InvitationStatus invitationStatus;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;

    @PrePersist
    public void prePersist() {
        if (this.invitationStatus == null) this.setInvitationStatus(InvitationStatus.PENDING);
    }

    public static FolderShareEntity create(FolderEntity folder, UserEntity ownerUser, UserEntity targetUser, FolderRoleEntity folderRole) {
        FolderShareEntity entity = new FolderShareEntity();

        entity.setFolder(folder);
        entity.setOwnerUser(ownerUser);
        entity.setTargetUser(targetUser);
        entity.setRole(folderRole);

        return entity;
    }
}

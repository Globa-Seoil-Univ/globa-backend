package org.y2k2.globa.factory;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(FolderShareRepositoryImpl.class)
@Component
public class FolderShareFactory extends CreatorFactory<FolderShareEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private FolderShareRepository folderShareRepository;

    private FolderEntity folder;
    private UserEntity owner;
    private UserEntity target;
    private FolderRoleEntity role;
    private InvitationStatus invitationStatus = InvitationStatus.ACCEPT;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected FolderShareEntity create() {
        return new FolderShareEntity();
    }

    @Override
    protected FolderShareEntity setDefaultValues(FolderShareEntity entity) {
        entity.setFolder(folder);
        entity.setOwnerUser(owner);
        entity.setTargetUser(target);
        entity.setInvitationStatus(invitationStatus);
        entity.setRole(role);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected FolderShareEntity saveEntity(FolderShareEntity entity) {
        if (folderShareRepository != null) {
            return folderShareRepository.save(entity);
        } else {
            throw new RuntimeException("FolderShareRepository is null, entity will not be persisted");
        }
    }
}

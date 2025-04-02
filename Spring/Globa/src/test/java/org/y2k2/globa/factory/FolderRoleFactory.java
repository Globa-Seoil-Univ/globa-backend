package org.y2k2.globa.factory;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folderrole.repository.FolderRoleRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(FolderRoleRepositoryImpl.class)
@Component
public class FolderRoleFactory extends CreatorFactory<FolderRoleEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private FolderRoleRepository folderShareRepository;

    private String roleId = "1";
    private String roleName = FolderRole.OWNER.getRoleName();

    @Override
    protected FolderRoleEntity create() {
        return new FolderRoleEntity();
    }

    @Override
    protected FolderRoleEntity setDefaultValues(FolderRoleEntity entity) {
        entity.setRoleId(roleId);
        entity.setRoleName(roleName);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());
        return entity;
    }

    @Override
    protected FolderRoleEntity saveEntity(FolderRoleEntity entity) {
        if (folderShareRepository != null) {
            return folderShareRepository.save(entity);
        } else {
            throw new RuntimeException("FolderShareRepository is null, entity will not be persisted");
        }
    }
}

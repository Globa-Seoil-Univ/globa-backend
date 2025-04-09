package org.y2k2.globa.factory.folderrole;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.type.FolderRole;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.folderrole.FolderRoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleRepositoryImpl;

@Slf4j
@Getter
@Setter
@Import(FolderRoleRepositoryImpl.class)
@Component
public class FolderRoleFactory extends AbstractFactory<FolderRoleEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private FolderRoleTestRepositoryImpl folderRoleRepository;

    private String roleName = FolderRole.OWNER.getRoleName();

    @Override
    protected FolderRoleEntity create() {
        return new FolderRoleEntity();
    }

    @Override
    protected FolderRoleEntity setDefaultValues(FolderRoleEntity entity) {
        entity.setRoleName(roleName);
        entity.setCreatedTime(new CustomTimestamp().getTimestamp());
        return entity;
    }

    @Override
    protected FolderRoleEntity saveEntity(FolderRoleEntity entity) {
        if (folderRoleRepository != null) {
            return folderRoleRepository.save(entity);
        } else {
            throw new RuntimeException("FolderRoleRepository is null, entity will not be persisted");
        }
    }
}

package org.y2k2.globa.fixture.folderrole;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.repository.FolderRoleTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;

@Import(FolderRoleTestRepositoryImpl.class)
@Component
public class FolderRoleFixture implements Fixture<FolderRoleEntity> {
    @Autowired
    private FolderRoleTestRepositoryImpl folderRoleFactory;

    @Override
    public FolderRoleEntity save(FolderRoleEntity entity) {
        return folderRoleFactory.save(entity);
    }

    public static FolderBuilder builder() {
        return new FolderBuilder();
    }

    public static class FolderBuilder {
        private FolderRole role;

        private FolderBuilder() {}

        public FolderBuilder role(FolderRole role) {
            this.role = role;
            return this;
        }

        public FolderRoleEntity build() {
            FolderRoleEntity folderRole = new FolderRoleEntity();
            folderRole.setRoleName(role);
            return folderRole;
        }
    }

    public FolderRoleEntity getEntity(FolderRole folderRole) {
        return folderRoleFactory.getRole(folderRole)
                .orElseThrow(() -> new IllegalArgumentException("Folder role not found: " + folderRole));
    }
}

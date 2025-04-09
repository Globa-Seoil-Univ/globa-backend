package org.y2k2.globa.fixture.folderrole;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.folderrole.FolderRoleFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;

@Component
@Getter
public class FolderRoleFixture extends AbstractFixture<FolderRoleEntity> {
    @Autowired
    private FolderRoleFactory folderRoleFactory;

    @Override
    protected FolderRoleEntity build() {
        return folderRoleFactory.createAndSave();
    }
}

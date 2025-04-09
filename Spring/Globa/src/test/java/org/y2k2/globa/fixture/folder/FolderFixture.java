package org.y2k2.globa.fixture.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.folder.FolderFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
public class FolderFixture extends AbstractFixture<FolderEntity> {
    @Autowired
    private FolderFactory folderFactory;

    @Override
    protected FolderEntity build() {
        return folderFactory.createAndSave();
    }

    public FolderFixture withUser(UserEntity user) {
        folderFactory.setUser(user);
        return this;
    }
}

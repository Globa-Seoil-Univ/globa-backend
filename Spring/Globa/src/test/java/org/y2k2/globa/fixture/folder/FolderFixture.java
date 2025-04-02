package org.y2k2.globa.fixture.folder;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.FolderFactory;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
@Getter
public class FolderFixture {
    @Autowired
    private FolderFactory folderFactory;

    public void createFixture(UserEntity user) {
        createFolder(user);
    }

    private void createFolder(UserEntity user) {
        folderFactory.setUser(user);
        folderFactory.createAndSave();
    }
}

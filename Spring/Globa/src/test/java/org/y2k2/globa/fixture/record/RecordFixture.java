package org.y2k2.globa.fixture.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.y2k2.globa.factory.record.RecordFactory;
import org.y2k2.globa.fixture.AbstractFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Component
public class RecordFixture extends AbstractFixture<RecordEntity> {
    @Autowired
    private RecordFactory recordFactory;

    @Override
    protected RecordEntity build() {
        return recordFactory.createAndSave();
    }

    public RecordFixture withUser(UserEntity user) {
        recordFactory.setUser(user);
        return this;
    }

    public RecordFixture withFolder(FolderEntity folder) {
        recordFactory.setFolder(folder);
        return this;
    }
}

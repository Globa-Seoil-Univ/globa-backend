package org.y2k2.globa.fixture.folder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(FolderRepositoryImpl.class)
@Component
public class FolderFixture implements Fixture<FolderEntity> {
    @Autowired
    private FolderRepository folderRepository;

    public static FolderBuilder builder() {
        return new FolderBuilder();
    }

    @Override
    public FolderEntity save(FolderEntity entity) {
        return folderRepository.save(entity);
    }

    public static class FolderBuilder {
        private String title = "Default Folder Title";
        private UserEntity user;

        private FolderBuilder() {}

        public FolderBuilder title(String title) {
            this.title = title;
            return this;
        }

        public FolderBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public FolderEntity build() {
            FolderEntity folder = new FolderEntity();
            folder.setTitle(title);
            folder.setUser(user);
            return folder;
        }
    }
}

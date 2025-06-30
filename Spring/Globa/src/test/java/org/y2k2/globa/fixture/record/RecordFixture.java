package org.y2k2.globa.fixture.record;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.type.Language;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(RecordRepositoryImpl.class)
@Component
public class RecordFixture implements Fixture<RecordEntity> {
    @Autowired
    private RecordRepository recordRepository;

    @Override
    public RecordEntity save(RecordEntity entity) {
        return recordRepository.save(entity);
    }

    public static RecordBuilder builder() {
        return new RecordBuilder();
    }

    public static class RecordBuilder {
        private String title = "Default Title";
        private UserEntity user;
        private FolderEntity folder;
        private String path = "/default/path";
        private Boolean isShare = false;

        private RecordBuilder() {}

        public RecordBuilder title(String title) {
            this.title = title;
            return this;
        }

        public RecordBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public RecordBuilder folder(FolderEntity folder) {
            this.folder = folder;
            return this;
        }

        public RecordBuilder path(String path) {
            this.path = path;
            return this;
        }

        public RecordBuilder isShare(Boolean isShare) {
            this.isShare = isShare;
            return this;
        }

        public RecordEntity build() {
            RecordEntity entity = new RecordEntity();
            entity.setTitle(title);
            entity.setUser(user);
            entity.setFolder(folder);
            entity.setPath(path);
            entity.setSize("1000");
            entity.setIsShare(isShare);
            entity.setLang(Language.KO);
            return entity;
        }
    }
}

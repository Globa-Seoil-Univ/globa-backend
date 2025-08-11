package org.y2k2.globa.fixture.dummyimage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
import org.y2k2.globa.infrastructure.persistence.dummyimage.repository.DummyImageRepositoryImpl;

@Import(DummyImageRepositoryImpl.class)
@Component
public class DummyImageFixture implements Fixture<DummyImageEntity> {
    @Autowired
    private DummyImageRepository dummyImageRepository;

    @Override
    public DummyImageEntity save(DummyImageEntity entity) {
        return dummyImageRepository.save(entity);
    }

    public static DummyImageBuilder builder() {
        return new DummyImageBuilder();
    }

    public static class DummyImageBuilder {
        private String path = "/dummies/default.png";
        private Long size = 1000L;
        private String type = "image/png";

        private DummyImageBuilder() {}

        public DummyImageBuilder path(String path) {
            this.path = path;
            return this;
        }

        public DummyImageBuilder size(Long size) {
            this.size = size;
            return this;
        }

        public DummyImageBuilder type(String type) {
            this.type = type;
            return this;
        }

        public DummyImageEntity build() {
            DummyImageEntity entity = new DummyImageEntity();
            entity.setImagePath(path);
            entity.setImageSize(size);
            entity.setImageType(type);

            return entity;
        }
    }
}

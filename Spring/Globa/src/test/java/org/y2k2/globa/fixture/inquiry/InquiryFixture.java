package org.y2k2.globa.fixture.inquiry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.repository.InquiryRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(InquiryRepositoryImpl.class)
@Component
public class InquiryFixture implements Fixture<InquiryEntity> {
    @Autowired
    private InquiryRepository inquiryRepository;

    @Override
    public InquiryEntity save(InquiryEntity entity) {
        return inquiryRepository.save(entity);
    }

    public static InquiryBuilder builder() {
        return new InquiryBuilder();
    }

    public static class InquiryBuilder {
        private UserEntity user;
        private String title = "Default Inquiry Title";
        private String content = "Default Inquiry Content";
        private Boolean isSolved = false;

        private InquiryBuilder() {}

        public InquiryBuilder title(String title) {
            this.title = title;
            return this;
        }

        public InquiryBuilder content(String content) {
            this.content = content;
            return this;
        }

        public InquiryBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public InquiryBuilder isSolved(Boolean isSolved) {
            this.isSolved = isSolved;
            return this;
        }

        public InquiryEntity build() {
            InquiryEntity entity = new InquiryEntity();
            entity.setTitle(title);
            entity.setContent(content);
            entity.setUser(user);
            entity.setIsSolved(isSolved);

            return entity;
        }
    }
}

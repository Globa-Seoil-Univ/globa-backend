package org.y2k2.globa.fixture.answer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.answer.repository.AnswerRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(AnswerRepositoryImpl.class)
@Component
public class AnswerFixture implements Fixture<AnswerEntity> {
    @Autowired
    private AnswerRepository answerRepository;

    @Override
    public AnswerEntity save(AnswerEntity entity) {
        return answerRepository.save(entity);
    }

    public static AnswerBuilder builder() {
        return new AnswerBuilder();
    }

    public static class AnswerBuilder {
        private UserEntity user;
        private InquiryEntity inquiry;
        private String title = "Default Answer Title";
        private String content = "Default Answer Content";

        private AnswerBuilder() {}

        public AnswerBuilder title(String title) {
            this.title = title;
            return this;
        }

        public AnswerBuilder content(String content) {
            this.content = content;
            return this;
        }

        public AnswerBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public AnswerBuilder inquiry(InquiryEntity inquiry) {
            this.inquiry = inquiry;
            return this;
        }

        public AnswerEntity build() {
            AnswerEntity entity = new AnswerEntity();
            entity.setTitle(title);
            entity.setContent(content);
            entity.setUser(user);
            entity.setInquiry(inquiry);

            return entity;
        }
    }
}

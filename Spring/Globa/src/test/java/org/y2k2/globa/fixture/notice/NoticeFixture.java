package org.y2k2.globa.fixture.notice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.notice.repository.NoticeRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Import(NoticeRepositoryImpl.class)
@Component
public class NoticeFixture implements Fixture<NoticeEntity> {
    @Autowired
    private NoticeRepository noticeRepository;

    @Override
    public NoticeEntity save(NoticeEntity entity) {
        return noticeRepository.save(entity);
    }

    public static NoticeBuilder builder() {
        return new NoticeBuilder();
    }

    public static class NoticeBuilder {
        private UserEntity user;
        private String title = "Default Title";
        private String content = "Default Content";
        private String bgColor = "#FFFFFF";
        private String thumbnail = "/default/thumbnail.png";

        private NoticeBuilder() {}

        public NoticeBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public NoticeBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NoticeBuilder content(String content) {
            this.content = content;
            return this;
        }

        public NoticeBuilder bgColor(String bgColor) {
            this.bgColor = bgColor;
            return this;
        }

        public NoticeBuilder thumbnail(String thumbnail) {
            this.thumbnail = thumbnail;
            return this;
        }

        public NoticeEntity build() {
            NoticeEntity notice = new NoticeEntity();
            notice.setUser(user);
            notice.setTitle(title);
            notice.setContent(content);
            notice.setBgColor(bgColor);
            notice.setThumbnailPath(thumbnail);
            notice.setThumbnailSize(100L);
            notice.setThumbnailType("image/png");

            return notice;
        }
    }
}

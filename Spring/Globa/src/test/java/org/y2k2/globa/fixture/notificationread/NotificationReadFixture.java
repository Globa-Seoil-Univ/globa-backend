package org.y2k2.globa.fixture.notificationread;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.domain.notificationread.repository.NotificationReadRepository;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.repository.NotificationTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.notificationread.NotificationReadTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.notificationread.entity.NotificationReadEntity;
import org.y2k2.globa.infrastructure.persistence.notificationread.repository.NotificationReadRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Import(NotificationReadTestRepositoryImpl.class)
@Component
public class NotificationReadFixture implements Fixture<NotificationReadEntity> {
    @Autowired
    private NotificationReadTestRepositoryImpl notificationReadRepository;

    @Override
    public NotificationReadEntity save(NotificationReadEntity entity) {
        return notificationReadRepository.save(entity);
    }

    public static NotificationReadBuilder builder() {
        return new NotificationReadBuilder();
    }

    public static class NotificationReadBuilder {
        private UserEntity user;
        private NotificationEntity notification;
        private Boolean isDeleted = false;

        public NotificationReadBuilder user(UserEntity user) {
            this.user = user;
            return this;
        }

        public NotificationReadBuilder notification(NotificationEntity notification) {
            this.notification = notification;
            return this;
        }

        public NotificationReadBuilder isDeleted(Boolean isDeleted) {
            this.isDeleted = isDeleted;
            return this;
        }

        public NotificationReadEntity build() {
            NotificationReadEntity entity = new NotificationReadEntity();
            entity.setUser(user);
            entity.setNotification(notification);
            entity.setIsDeleted(isDeleted);

            return entity;
        }
    }
}

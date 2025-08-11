package org.y2k2.globa.fixture.notification;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.fixture.Fixture;
import org.y2k2.globa.infrastructure.persistence.comment.entity.CommentEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;
import org.y2k2.globa.infrastructure.persistence.notification.repository.NotificationTestRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Import(NotificationTestRepositoryImpl.class)
@Component
public class NotificationFixture implements Fixture<NotificationEntity> {
    @Autowired
    private NotificationTestRepositoryImpl notificationRepository;

    @Override
    public NotificationEntity save(NotificationEntity entity) {
        return notificationRepository.save(entity);
    }

    public List<NotificationEntity> saveAll(List<NotificationEntity> entities) {
        return notificationRepository.saveAll(entities);
    }

    public static NotificationBuilder builder() {
        return new NotificationBuilder();
    }

    public static class NotificationBuilder {
        private NotificationType type;
        private UserEntity receiver;
        private UserEntity sender;
        private FolderShareEntity folderShare;
        private FolderEntity folder;
        private RecordEntity record;
        private CommentEntity comment;
        private NoticeEntity notice;
        private InquiryEntity inquiry;

        public NotificationBuilder type(NotificationType type) {
            this.type = type;
            return this;
        }

        public NotificationBuilder receiver(UserEntity receiver) {
            this.receiver = receiver;
            return this;
        }

        public NotificationBuilder sender(UserEntity sender) {
            this.sender = sender;
            return this;
        }

        public NotificationBuilder folderShare(FolderShareEntity folderShare) {
            this.folderShare = folderShare;
            return this;
        }

        public NotificationBuilder folder(FolderEntity folder) {
            this.folder = folder;
            return this;
        }

        public NotificationBuilder record(RecordEntity record) {
            this.record = record;
            return this;
        }

        public NotificationBuilder comment(CommentEntity comment) {
            this.comment = comment;
            return this;
        }

        public NotificationBuilder notice(NoticeEntity notice) {
            this.notice = notice;
            return this;
        }

        public NotificationBuilder inquiry(InquiryEntity inquiry) {
            this.inquiry = inquiry;
            return this;
        }

        public NotificationEntity build() {
            NotificationEntity entity = new NotificationEntity();
            entity.setType(type);
            entity.setReceiver(receiver);
            entity.setSender(sender);
            entity.setFolderShare(folderShare);
            entity.setFolder(folder);
            entity.setRecord(record);
            entity.setComment(comment);
            entity.setNotice(notice);
            entity.setInquiry(inquiry);
            return entity;
        }
    }
}

package org.y2k2.globa.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "notification")
public class NotificationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id", columnDefinition = "INT UNSIGNED")
    private long notificationId;

    @Column(name = "type_id", nullable = false)
    @Check(constraints = "CHECK (type_id >= 1 AND type_id <= 8)")
    private char typeId;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "to_user_id", referencedColumnName = "user_id")
    private UserEntity toUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "from_user_id", referencedColumnName = "user_id")
    private UserEntity fromUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "share_id", referencedColumnName = "share_id")
    private FolderShareEntity folderShare;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "folder_id", referencedColumnName = "folder_id")
    private FolderEntity folder;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "record_id", referencedColumnName = "record_id")
    private RecordEntity record;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "comment_id", referencedColumnName = "comment_id")
    private CommentEntity comment;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "notice_id", referencedColumnName = "notice_id")
    private NoticeEntity notice;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "inquiry_id", referencedColumnName = "inquiry_id")
    private InquiryEntity inquiry;


    @Column(name = "is_read")
    @ColumnDefault("false")
    private Boolean isRead;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;
}

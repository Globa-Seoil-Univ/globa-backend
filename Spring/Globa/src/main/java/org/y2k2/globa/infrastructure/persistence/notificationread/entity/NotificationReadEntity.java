package org.y2k2.globa.infrastructure.persistence.notificationread.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.notification.entity.NotificationEntity;

import java.time.LocalDateTime;


@Getter
@Setter
@Entity
@Table(name = "notification_read")
@NoArgsConstructor
public class NotificationReadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "read_id", columnDefinition = "INT UNSIGNED")
    private Long read_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false, columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notification_id", columnDefinition = "INT UNSIGNED")
    private NotificationEntity notification;

    @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}

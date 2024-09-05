package org.y2k2.globa.entity;

import jakarta.persistence.*;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@Table(name = "notification_read")
@NoArgsConstructor
public class NotificationReadEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "read_id", columnDefinition = "INT UNSIGNED")
    private Long read_id;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "notification_id", referencedColumnName = "notification_id")
    private NotificationEntity notification;

    @Column(name = "is_deleted")
    @ColumnDefault("false")
    private Boolean isDeleted;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @Builder
    public NotificationReadEntity(UserEntity user, NotificationEntity notification, Boolean isDeleted) {
        this.user = user;
        this.notification = notification;
        this.isDeleted = isDeleted;
    }
}

package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.y2k2.globa.converter.SnsKindConverter;
import org.y2k2.globa.type.SnsKind;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name = "app_user")
public class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", columnDefinition = "INT UNSIGNED")
    private Long userId;

    @Convert(converter = SnsKindConverter.class)
    @Column(name = "sns_kind", nullable = false, length = 4)
    private SnsKind snsKind;

    @Column(name = "sns_id", nullable = false, unique = true, length = 50)
    private String snsId;

    @Column(name = "code", nullable = false, unique = true, length = 6)
    private String code;

    @Column(name = "name", nullable = false, length = 50)
    private String name;

    @Column(name = "primary_nofi", columnDefinition = "DEFAULT 0")
    private Boolean primaryNofi;

    @Column(name = "upload_nofi", columnDefinition = "DEFAULT 0")
    private Boolean uploadNofi;

    @Column(name = "share_nofi", columnDefinition = "DEFAULT 0")
    private Boolean shareNofi;

    @Column(name = "event_nofi", columnDefinition = "DEFAULT 0")
    private Boolean eventNofi;

    @Column(name = "profile_size", columnDefinition = "INT UNSIGNED")
    private Long profileSize;

    @Column(name = "profile_type", length = 20)
    private String profileType;

    @Column(name = "profile_path", length = 200)
    private String profilePath;

    @Column(name = "notification_token", unique = true, length = 300)
    private String notificationToken;

    @Column(name = "notification_token_time")
    private LocalDateTime notificationTokenTime;

    @Column(name = "is_deleted", columnDefinition = "DEFAULT 0")
    private Boolean isDeleted;

    @Column(name = "deleted_time")
    private LocalDateTime deletedTime;

    @Column(name = "created_time", columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdTime;
}

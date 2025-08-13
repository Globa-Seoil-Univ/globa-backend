package org.y2k2.globa.infrastructure.persistence.inquiry.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="inquiry")
public class InquiryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id", columnDefinition = "INT UNSIGNED")
    private Long inquiryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", columnDefinition = "INT UNSIGNED")
    private UserEntity user;

    @Column(name = "title", nullable = false, length = 80)
    private String title;

    @Lob
    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(name = "is_solved", columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isSolved;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public static InquiryEntity create(UserEntity writer, String title, String content) {
        InquiryEntity entity = new InquiryEntity();

        entity.setUser(writer);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setIsSolved(false);

        return entity;
    }
}

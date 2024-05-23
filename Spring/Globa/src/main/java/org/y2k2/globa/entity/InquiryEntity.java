package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity
@Table(name="inquiry")
public class InquiryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "inquiry_id", columnDefinition = "INT UNSIGNED")
    private long inquiryId;

    @ManyToOne(fetch = FetchType.EAGER)
    @OnDelete(action = OnDeleteAction.SET_NULL)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @ColumnDefault("0")
    @Column(name = "is_solved")
    private boolean solved;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public static InquiryEntity create(UserEntity writer, String title, String content) {
        InquiryEntity entity = new InquiryEntity();

        entity.setUser(writer);
        entity.setTitle(title);
        entity.setContent(content);
        entity.setSolved(false);

        return entity;
    }
}

package org.y2k2.globa.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Getter
@Setter
@Entity(name="answer")
@Table(name="answer")
public class AnswerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "answer_id", columnDefinition = "INT UNSIGNED")
    private long answerId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "user_id")
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "inquiry_id", referencedColumnName = "inquiry_id")
    private InquiryEntity inquiry;

    @Column(name = "title", nullable = false)
    private String title;

    @Lob
    @Column(name = "content", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "created_time")
    private LocalDateTime createdTime;

    public static AnswerEntity create(UserEntity writer, InquiryEntity inquiry, String title, String content) {
        AnswerEntity entity = new AnswerEntity();

        entity.setUser(writer);
        entity.setInquiry(inquiry);
        entity.setTitle(title);
        entity.setContent(content);

        return entity;
    }
}

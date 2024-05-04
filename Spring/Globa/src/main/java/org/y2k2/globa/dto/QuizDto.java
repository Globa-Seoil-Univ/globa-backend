package org.y2k2.globa.dto;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.y2k2.globa.entity.FolderEntity;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.UserEntity;

import java.io.Serializable;
import java.time.LocalDateTime;

@AllArgsConstructor
@Getter
public class QuizDto implements Serializable {
    private Long quizId;
    private String question;
    private int answer;
}

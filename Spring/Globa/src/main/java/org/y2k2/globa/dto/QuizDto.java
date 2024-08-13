package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serializable;

@AllArgsConstructor
@Getter
public class QuizDto implements Serializable {
    private Long quizId;
    private String question;
    private int answer;
}

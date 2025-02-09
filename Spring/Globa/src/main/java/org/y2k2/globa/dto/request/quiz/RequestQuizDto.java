package org.y2k2.globa.dto.request.quiz;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestQuizDto {
    @AllArgsConstructor
    @Getter
    @Setter
    public static class Quiz implements Serializable {
        private Long quizId;
        private Boolean isCorrect;
    }

    private List<Quiz> quizs;
}
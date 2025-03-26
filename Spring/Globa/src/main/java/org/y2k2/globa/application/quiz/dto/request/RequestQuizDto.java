package org.y2k2.globa.application.quiz.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RequestQuizDto {
    public record Quiz(
            @NotNull(message = "퀴즈 ID는 필수입니다.")
            Long quizId,
            @NotNull(message = "퀴즈 내용은 필수입니다.")
            Boolean isCorrect
    ) {}

    @Valid
    @NotNull(message = "퀴즈 정답 결과는 필수입니다.")
    private List<Quiz> quizzes;
}
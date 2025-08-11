package org.y2k2.globa.application.quiz.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.quiz.dto.response.ResponseQuizzesDto;
import org.y2k2.globa.application.quiz.mapper.QuizMapper;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetQuizzesService {
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final QuizRepository quizRepository;

    public ResponseQuizzesDto get(Long folderId, Long recordId, Long userId) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(userId, folderId)
        );

        List<QuizEntity> quizzes = quizRepository.getAllQuizzes(recordId);
        if (quizzes.isEmpty()) {
            return new ResponseQuizzesDto(List.of());
        }

        return new ResponseQuizzesDto(
                quizzes.stream()
                        .map(QuizMapper.INSTANCE::toQuizDto)
                        .toList()
        );
    }
}

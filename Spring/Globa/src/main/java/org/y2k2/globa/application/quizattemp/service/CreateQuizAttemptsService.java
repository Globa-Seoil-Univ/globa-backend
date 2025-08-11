package org.y2k2.globa.application.quizattemp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.application.quizattemp.mapper.QuizAttemptMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.quiz.repository.QuizRepository;
import org.y2k2.globa.domain.quizattemp.repository.QuizAttemptRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CreateQuizAttemptsService {
    private final FindUserUseCase findUserUseCase;
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;

    private final RecordRepository recordRepository;
    private final QuizRepository quizRepository;
    private final QuizAttemptRepository quizAttemptRepository;

    public void create(Long folderId, Long recordId, RequestQuizDto dto, Long userId) {
        verifyFolderAccessibleUseCase.execute(
                VerifyFolderCommand.of(userId, folderId)
        );

        UserEntity user = findUserUseCase.execute(userId);

        RecordEntity record = recordRepository.getRecord(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        List<QuizEntity> quizzes = quizRepository.getAllByQuizzesInRecord(
                record,
                dto.getQuizzes().stream()
                        .map(RequestQuizDto.Quiz::quizId)
                        .toList()
        );

        List<QuizAttemptEntity> attempts = new ArrayList<>();

        boolean isNotExists = dto.getQuizzes().stream().anyMatch(
                q -> quizzes.stream().noneMatch(quiz -> quiz.getQuizId().equals(q.quizId()))
        );
        if (isNotExists) {
            throw new CustomException(ErrorCode.MISMATCH_QUIZ_RECORD_ID);
        }

        for (QuizEntity quiz : quizzes) {
            RequestQuizDto.Quiz quizDto = dto.getQuizzes().stream()
                    .filter(q -> q.quizId().equals(quiz.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_QUIZ));

            attempts.add(QuizAttemptMapper.INSTANCE.toEntity(quiz, user, quizDto.isCorrect()));
        }

        quizAttemptRepository.saveAll(attempts);
    }
}
package org.y2k2.globa.application.quizattemp.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.quiz.dto.request.RequestQuizDto;
import org.y2k2.globa.infrastructure.persistence.quizattemp.entity.QuizAttemptEntity;
import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.application.quizattemp.mapper.QuizAttemptMapper;
import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quizattemp.repository.QuizAttemptJpaRepository;
import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordJpaRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizAttemptService {
    private final QuizAttemptJpaRepository quizAttemptJpaRepository;
    private final QuizJpaRepository quizJpaRepository;
    private final FolderShareJpaRepository folderShareJpaRepository;
    private final RecordJpaRepository recordJpaRepository;

    @Transactional
    public void createQuizAttempts(Long recordId, Long folderId, RequestQuizDto dto, UserEntity user){
        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        RecordEntity record = recordJpaRepository.findByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        List<QuizEntity> quizzes = quizJpaRepository.findAllByRecordAndQuizIdIn(
                record,
                dto.getQuizzes().stream()
                    .map(RequestQuizDto.Quiz::quizId)
                    .toList()
        );

        List<QuizAttemptEntity> attempts = new ArrayList<>();

        boolean isNotExists = dto.getQuizzes().stream().anyMatch(
            q -> quizzes.stream().noneMatch(quiz -> quiz.getQuizId().equals(q.quizId()))
        );

        if (isNotExists) throw new CustomException(ErrorCode.MISMATCH_QUIZ_RECORD_ID);

        for (QuizEntity quiz : quizzes) {
            RequestQuizDto.Quiz quizDto = dto.getQuizzes().stream()
                    .filter(q -> q.quizId().equals(quiz.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_QUIZ));

            attempts.add(QuizAttemptMapper.INSTANCE.toEntity(quiz, user, quizDto.isCorrect()));
        }

        quizAttemptJpaRepository.saveAll(attempts);
    }
}

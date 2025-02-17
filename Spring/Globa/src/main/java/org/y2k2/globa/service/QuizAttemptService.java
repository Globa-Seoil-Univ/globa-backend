package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.quiz.QuizDto;
import org.y2k2.globa.dto.request.quiz.RequestQuizDto;
import org.y2k2.globa.entity.QuizAttemptEntity;
import org.y2k2.globa.entity.QuizEntity;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.QuizAttemptMapper;
import org.y2k2.globa.mapper.QuizMapper;
import org.y2k2.globa.repository.FolderShareRepository;
import org.y2k2.globa.repository.QuizAttemptRepository;
import org.y2k2.globa.repository.QuizRepository;
import org.y2k2.globa.repository.RecordRepository;
import org.y2k2.globa.type.InvitationStatus;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizAttemptService {
    private final QuizAttemptRepository quizAttemptRepository;
    private final QuizRepository quizRepository;
    private final FolderShareRepository folderShareRepository;
    private final RecordRepository recordRepository;

    @Transactional
    public void createQuizAttempts(Long recordId, Long folderId, RequestQuizDto dto, UserEntity user){
        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        RecordEntity record = recordRepository.findFirstByRecordId(recordId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_RECORD));

        List<QuizEntity> quizzes = quizRepository.findAllByRecordAndQuizIdIn(
                record,
            dto.getQuizzes().stream()
                .map(RequestQuizDto.Quiz::quizId)
                .toList()
        );

        List<QuizAttemptEntity> attempts = new ArrayList<>();

        for (QuizEntity quiz : quizzes) {
            if (dto.getQuizzes().stream().noneMatch(q -> q.quizId().equals(quiz.getQuizId()))) {
                throw new CustomException(ErrorCode.NOT_FOUND_QUIZ);
            }

            RequestQuizDto.Quiz quizDto = dto.getQuizzes().stream()
                    .filter(q -> q.quizId().equals(quiz.getQuizId()))
                    .findFirst()
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_QUIZ));

            attempts.add(QuizAttemptMapper.INSTANCE.toEntity(quiz, user, quizDto.isCorrect()));
        }

        quizAttemptRepository.saveAll(attempts);
    }
}

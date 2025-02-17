package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.quiz.QuizDto;
import org.y2k2.globa.entity.QuizEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.QuizMapper;
import org.y2k2.globa.repository.FolderShareRepository;
import org.y2k2.globa.repository.QuizRepository;
import org.y2k2.globa.type.InvitationStatus;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class QuizService {
    private final QuizRepository quizRepository;
    private final FolderShareRepository folderShareRepository;

    public List<QuizDto> getQuizzes(Long recordId, Long folderId, UserEntity user) {
        boolean hasAccess = folderShareRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);

        List<QuizEntity> quizzes = quizRepository.findAllByRecordRecordId(recordId);
        if(quizzes.isEmpty())
            throw new CustomException(ErrorCode.NOT_FOUND_QUIZ);

        return quizzes.stream()
                .map(QuizMapper.INSTANCE::toQuizDto)
                .toList();
    }
}

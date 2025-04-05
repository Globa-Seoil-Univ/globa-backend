//package org.y2k2.globa.application.quiz.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.y2k2.globa.application.quiz.dto.common.QuizDto;
//import org.y2k2.globa.infrastructure.persistence.quiz.entity.QuizEntity;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.application.quiz.mapper.QuizMapper;
//import org.y2k2.globa.infrastructure.persistence.foldershare.repository.FolderShareJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.quiz.repository.QuizJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
//
//import java.util.List;
//
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class QuizService {
//    private final QuizJpaRepository quizJpaRepository;
//    private final FolderShareJpaRepository folderShareJpaRepository;
//
//    public List<QuizDto> getQuizzes(Long recordId, Long folderId, UserEntity user) {
//        boolean hasAccess = folderShareJpaRepository.existsByTargetUserAndFolderFolderIdAndInvitationStatus(user, folderId, InvitationStatus.ACCEPT);
//        if (!hasAccess) throw new CustomException(ErrorCode.NOT_DESERVE_ACCESS_FOLDER);
//
//        List<QuizEntity> quizzes = quizJpaRepository.findAllByRecordRecordId(recordId);
//        if(quizzes.isEmpty())
//            throw new CustomException(ErrorCode.NOT_FOUND_QUIZ);
//
//        return quizzes.stream()
//                .map(QuizMapper.INSTANCE::toQuizDto)
//                .toList();
//    }
//}

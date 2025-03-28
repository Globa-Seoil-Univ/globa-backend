//package org.y2k2.globa.application.answer.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
//import org.y2k2.globa.infrastructure.persistence.answer.repository.AnswerJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
//import org.y2k2.globa.infrastructure.persistence.inquiry.repository.InquiryJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
//import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInquiryDto;
//import org.y2k2.globa.application.answer.mapper.AnswerMapper;
//import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
//import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
//import org.y2k2.globa.application.notification.service.NotificationService;
//import org.y2k2.globa.application.userrole.service.UserRoleService;
//
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class AnswerService {
//    private final ApplicationEventPublisher publisher;
//
//    private final UserRoleService userRoleService;
//
//    private final NotificationService notificationService;
//
//    private final AnswerJpaRepository answerJpaRepository;
//    private final InquiryJpaRepository inquiryJpaRepository;
//    private final UserRoleJpaRepository userRoleJpaRepository;
//
//    public void addAnswer(long inquiryId, RequestAnswerDto dto, UserEntity user) {
//        validateRole(user);
//
//        InquiryEntity inquiry = inquiryJpaRepository.findByInquiryId(inquiryId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
//        if (inquiry.getIsSolved()) throw new CustomException(ErrorCode.INQUIRY_ANSWER_DUPLICATED);
//        inquiry.setIsSolved(true);
//
//        AnswerEntity answer = AnswerMapper.INSTANCE.toEntity(user, inquiry, dto);
//
//        inquiryJpaRepository.save(inquiry);
//        answerJpaRepository.save(answer);
//
//        RequestNotificationWithInquiryDto info = RequestNotificationWithInquiryDto.builder()
//                .sender(user)
//                .receiver(user)
//                .title("문의 답변 도착!")
//                .body(inquiry.getTitle() + "에 대한 문의 답변이 도착했어요!")
//                .inquiry(inquiry)
//                .notificationType(NotificationType.INQUIRY)
//                .build();
//
//        notificationService.saveNotification(info);
//        publisher.publishEvent(info);
//    }
//
//    public void editAnswer(long inquiryId, long answerId, RequestAnswerDto dto, UserEntity user) {
//        validateRole(user);
//
//        InquiryEntity inquiry = inquiryJpaRepository.findByInquiryId(inquiryId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
//
//        AnswerEntity answer = answerJpaRepository.findByAnswerId(answerId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));
//
//        if (!inquiry.getIsSolved()) {
//            inquiry.setIsSolved(true);
//        }
//
//        answer.setUser(user);
//        answer.setTitle(dto.getTitle());
//        answer.setContent(dto.getContent());
//
//        inquiryJpaRepository.save(inquiry);
//        answerJpaRepository.save(answer);
//    }
//
//    public void deleteAnswer(long inquiryId, long answerId, UserEntity user) {
//        validateRole(user);
//
//        InquiryEntity inquiry = inquiryJpaRepository.findByInquiryId(inquiryId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
//
//        AnswerEntity answer = answerJpaRepository.findByAnswerId(answerId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));
//
//        if (inquiry.getIsSolved()) {
//            inquiry.setIsSolved(false);
//        }
//
//        inquiryJpaRepository.save(inquiry);
//        answerJpaRepository.delete(answer);
//    }
//
//    private void validateRole(UserEntity user) {
//        Optional<UserRoleEntity> optionalUserRole = userRoleJpaRepository.findByUser(user);
//
//        if (optionalUserRole.isEmpty()) {
//            userRoleService.createUserRoleAndThrowException(user);
//        } else {
//            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
//            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
//        }
//    }
//}

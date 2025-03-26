package org.y2k2.globa.application.answer.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInquiryDto;
import org.y2k2.globa.application.answer.mapper.AnswerMapper;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.common.type.NotificationType;
import org.y2k2.globa.application.notification.service.NotificationService;
import org.y2k2.globa.application.userrole.service.UserRoleService;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AnswerService {
    private final ApplicationEventPublisher publisher;

    private final UserRoleService userRoleService;

    private final NotificationService notificationService;

    private final AnswerRepository answerRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRoleRepository userRoleRepository;

    public void addAnswer(long inquiryId, RequestAnswerDto dto, UserEntity user) {
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
        if (inquiry.getIsSolved()) throw new CustomException(ErrorCode.INQUIRY_ANSWER_DUPLICATED);
        inquiry.setIsSolved(true);

        AnswerEntity answer = AnswerMapper.INSTANCE.toEntity(user, inquiry, dto);

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);

        RequestNotificationWithInquiryDto info = RequestNotificationWithInquiryDto.builder()
                .sender(user)
                .receiver(user)
                .title("문의 답변 도착!")
                .body(inquiry.getTitle() + "에 대한 문의 답변이 도착했어요!")
                .inquiry(inquiry)
                .notificationType(NotificationType.INQUIRY)
                .build();

        notificationService.saveNotification(info);
        publisher.publishEvent(info);
    }

    public void editAnswer(long inquiryId, long answerId, RequestAnswerDto dto, UserEntity user) {
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));

        AnswerEntity answer = answerRepository.findByAnswerId(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

        if (!inquiry.getIsSolved()) {
            inquiry.setIsSolved(true);
        }

        answer.setUser(user);
        answer.setTitle(dto.getTitle());
        answer.setContent(dto.getContent());

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);
    }

    public void deleteAnswer(long inquiryId, long answerId, UserEntity user) {
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));

        AnswerEntity answer = answerRepository.findByAnswerId(answerId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

        if (inquiry.getIsSolved()) {
            inquiry.setIsSolved(false);
        }

        inquiryRepository.save(inquiry);
        answerRepository.delete(answer);
    }

    private void validateRole(UserEntity user) {
        Optional<UserRoleEntity> optionalUserRole = userRoleRepository.findByUser(user);

        if (optionalUserRole.isEmpty()) {
            userRoleService.createUserRoleAndThrowException(user);
        } else {
            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
        }
    }
}

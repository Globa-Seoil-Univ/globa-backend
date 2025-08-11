package org.y2k2.globa.application.answer.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.answer.dto.request.RequestAnswerDto;
import org.y2k2.globa.application.answer.mapper.AnswerMapper;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithInquiryDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateAnswerService {
    private final ApplicationEventPublisher publisher;

    private final VerifyUserWritableUseCase verifyUserWritableUseCase;
    private final FindUserUseCase findUserUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;

    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;

    @Transactional
    public void create(Long inquiryId, RequestAnswerDto dto, Long userId) {
        verifyUserWritableUseCase.execute(userId);

        InquiryEntity inquiry = inquiryRepository.getInquiry(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));

        if (inquiry.getIsSolved()) {
            throw new CustomException(ErrorCode.INQUIRY_ANSWER_DUPLICATED);
        }
        inquiry.setIsSolved(true);

        UserEntity user = findUserUseCase.execute(userId);
        AnswerEntity answer = AnswerMapper.INSTANCE.toEntity(user, inquiry, dto);

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);

        createAndSendNotification(user, inquiry);
    }

    private void createAndSendNotification(UserEntity user, InquiryEntity inquiry) {
        RequestNotificationWithInquiryDto info = RequestNotificationWithInquiryDto.builder()
                .sender(user)
                .receiver(user)
                .title("문의 답변 도착!")
                .body(inquiry.getTitle() + "에 대한 문의 답변이 도착했어요!")
                .inquiry(inquiry)
                .notificationType(NotificationType.INQUIRY)
                .build();

        createNotificationUseCase.execute(CreateNotificationCommand.of(info));
        publisher.publishEvent(info);
    }
}

package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.common.answer.RequestAnswerDto;
import org.y2k2.globa.dto.request.notification.RequestNotificationWithInquiryDto;
import org.y2k2.globa.dto.common.role.UserRole;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.type.NotificationType;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {
    private final ApplicationEventPublisher publisher;

    private final NotificationService notificationService;

    private final AnswerRepository answerRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    @Transactional
    public void addAnswer(long userId, long inquiryId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new CustomException(ErrorCode.NOT_FOUND_INQUIRY);
        if (inquiry.getIsSolved()) throw new CustomException(ErrorCode.INQUIRY_ANSWER_DUPLICATED);

        inquiry.setIsSolved(true);
        AnswerEntity answer = AnswerEntity.create(user, inquiry, dto.getTitle(), dto.getContent());

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

    @Transactional
    public void editAnswer(long userId, long inquiryId, long answerId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new CustomException(ErrorCode.NOT_FOUND_INQUIRY);

        AnswerEntity answer = validateAnswer(answerId);

        if (!inquiry.getIsSolved()) inquiry.setIsSolved(true);
        answer.setUser(user);
        answer.setTitle(dto.getTitle());
        answer.setContent(dto.getContent());

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);
    }

    @Transactional
    public void deleteAnswer(long userId, long inquiryId, long answerId) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new CustomException(ErrorCode.NOT_FOUND_INQUIRY);

        AnswerEntity answer = validateAnswer(answerId);

        if (inquiry.getIsSolved()) inquiry.setIsSolved(false);

        inquiryRepository.save(inquiry);
        answerRepository.delete(answer);
    }

    private UserEntity validateUser(long userId) {
        UserEntity user = userRepository.findByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
        if (user.getIsDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        return user;
    }

    private void validateRole(UserEntity user) {
        UserRoleEntity role = userRoleRepository.findByUser(user);
        if (role == null) throw new CustomException(ErrorCode.NOT_NULL_ROLE);

        String roleName = role.getRoleId().getName();
        boolean isValid = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isValid) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);

    }

    private AnswerEntity validateAnswer(long answerId) {
        AnswerEntity answer = answerRepository.findByAnswerId(answerId);
        if (answer == null) throw new CustomException(ErrorCode.NOT_FOUND_ANSWER);

        return answer;
    }
}

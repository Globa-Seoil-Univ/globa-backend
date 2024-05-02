package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.*;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.DuplicatedExcepiton;
import org.y2k2.globa.exception.ForbiddenException;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.exception.NotFoundException;
import org.y2k2.globa.mapper.NotificationMapper;
import org.y2k2.globa.repository.*;

@Service
@RequiredArgsConstructor
public class AnswerService {
    private final AnswerRepository answerRepository;
    private final InquiryRepository inquiryRepository;
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final NotificationRepository notificationRepository;

    @Transactional
    public void addAnswer(long userId, long inquiryId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");
        if (inquiry.isSolved()) throw new DuplicatedExcepiton("Answer already exists");

        inquiry.setSolved(true);
        AnswerEntity answer = AnswerEntity.create(user, inquiry, dto.getTitle(), dto.getContent());

        inquiryRepository.save(inquiry);
        answerRepository.save(answer);

        NotificationEntity notification = NotificationMapper.INSTANCE.toNotificationWithInquiry(
                new RequestNotificationWithInquiryDto(user, inquiry.getUser(), inquiry)
        );
        notification.setTypeId(NotificationTypeEnum.INQUIRY.getTypeId());
        notificationRepository.save(notification);
    }

    @Transactional
    public void editAnswer(long userId, long inquiryId, long answerId, RequestAnswerDto dto) {
        UserEntity user = validateUser(userId);
        validateRole(user);

        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId);
        if (inquiry == null) throw new NotFoundException("Not found inquiry");

        AnswerEntity answer = validateAnswer(answerId);

        if (!inquiry.isSolved()) inquiry.setSolved(true);
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
        if (inquiry == null) throw new NotFoundException("Not found inquiry");

        AnswerEntity answer = validateAnswer(answerId);

        if (inquiry.isSolved()) inquiry.setSolved(false);

        inquiryRepository.save(inquiry);
        answerRepository.delete(answer);
    }

    private UserEntity validateUser(long userId) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");

        return user;
    }

    private void validateRole(UserEntity user) {
        UserRoleEntity role = userRoleRepository.findByUser(user);
        if (role == null) throw new ForbiddenException("Wrong approach");

        String roleName = role.getRoleId().getName();
        boolean isValid = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isValid) throw new ForbiddenException("Only admin or editor can write answers");

    }

    private AnswerEntity validateAnswer(long answerId) {
        AnswerEntity answer = answerRepository.findByAnswerId(answerId);
        if (answer == null) throw new NotFoundException("Not found answer");

        return answer;
    }
}

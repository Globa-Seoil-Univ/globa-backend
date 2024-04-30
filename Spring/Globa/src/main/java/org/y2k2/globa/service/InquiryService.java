package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.RequestInquiryDto;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.InvalidTokenException;
import org.y2k2.globa.repository.AnswerRepository;
import org.y2k2.globa.repository.InquiryRepository;
import org.y2k2.globa.repository.UserRepository;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;
    private final UserRepository userRepository;

    public long addInquiry(long userId, RequestInquiryDto dto) {
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) throw new InvalidTokenException("Not found user");

        InquiryEntity inquiry = InquiryEntity.create(user, dto.getTitle(), dto.getContent());
        InquiryEntity response = inquiryRepository.save(inquiry);

        return response.getInquiryId();
    }
}

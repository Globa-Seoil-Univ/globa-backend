package org.y2k2.globa.application.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.inquiry.dto.request.RequestInquiryDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateInquiryService {
    private final FindUserUseCase findUserUseCase;

    private final InquiryRepository inquiryRepository;

    public Long create(RequestInquiryDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);

        InquiryEntity inquiry = InquiryEntity.create(user, dto.title(), dto.content());
        InquiryEntity response = inquiryRepository.save(inquiry);
        return response.getInquiryId();
    }
}

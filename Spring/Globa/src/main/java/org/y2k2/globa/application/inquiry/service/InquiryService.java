package org.y2k2.globa.application.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.application.inquiry.dto.common.InquiryDto;
import org.y2k2.globa.application.inquiry.dto.request.InquiryPaginationDto;
import org.y2k2.globa.application.inquiry.dto.request.RequestInquiryDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDto;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.application.inquiry.mapper.InquiryMapper;
import org.y2k2.globa.infrastructure.persistence.answer.repository.AnswerJpaRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.repository.InquiryJpaRepository;
import org.y2k2.globa.common.type.InquirySort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryJpaRepository inquiryJpaRepository;
    private final AnswerJpaRepository answerJpaRepository;

    public ResponseInquiryDto getInquiries(InquiryPaginationDto pagination, UserEntity user) {
        Pageable pageable = PageRequest.of(pagination.page() - 1, pagination.count());
        Page<InquiryEntity> inquiryEntityPage;

        if (pagination.sort().equals(InquirySort.S)) {
            inquiryEntityPage = inquiryJpaRepository.findAllByUserAndIsSolvedIsTrueOrderByCreatedTimeDesc(user, pageable);
        } else if (pagination.sort().equals(InquirySort.N)) {
            inquiryEntityPage = inquiryJpaRepository.findAllByUserAndIsSolvedIsFalseOrderByCreatedTimeDesc(user, pageable);
        } else {
            inquiryEntityPage = inquiryJpaRepository.findAllByUserOrderByCreatedTimeDesc(user, pageable);
        }

        List<InquiryEntity> inquiryEntities = inquiryEntityPage.getContent();
        List<InquiryDto> dtos = inquiryEntities.stream()
                .map(InquiryMapper.INSTANCE::toInquiryDto)
                .toList();

        return new ResponseInquiryDto(dtos, inquiryEntityPage.getTotalElements());
    }

    public ResponseInquiryDetailDto getInquiry(long inquiryId, UserEntity user) {
        InquiryEntity inquiry = inquiryJpaRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
        if (!inquiry.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_INQUIRY_OWNER);
        }

        if (inquiry.getIsSolved()) {
            AnswerEntity answer = answerJpaRepository.findByInquiry(inquiry)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

            return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry, answer);
        }

        return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry);
    }

    public long addInquiry(RequestInquiryDto dto, UserEntity user) {
        InquiryEntity inquiry = InquiryEntity.create(user, dto.title(), dto.content());
        InquiryEntity response = inquiryJpaRepository.save(inquiry);
        return response.getInquiryId();
    }
}

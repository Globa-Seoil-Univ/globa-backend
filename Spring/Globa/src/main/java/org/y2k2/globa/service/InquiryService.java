package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.common.inquiry.InquiryDto;
import org.y2k2.globa.dto.request.inquiry.InquiryPaginationDto;
import org.y2k2.globa.dto.request.inquiry.RequestInquiryDto;
import org.y2k2.globa.dto.response.inquiry.ResponseInquiryDetailDto;
import org.y2k2.globa.dto.response.inquiry.ResponseInquiryDto;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.mapper.InquiryMapper;
import org.y2k2.globa.repository.AnswerRepository;
import org.y2k2.globa.repository.InquiryRepository;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.type.InquirySort;

import java.util.List;

@Service
@RequiredArgsConstructor
public class InquiryService {
    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;

    public ResponseInquiryDto getInquiries(InquiryPaginationDto pagination, UserEntity user) {
        Pageable pageable = PageRequest.of(pagination.page() - 1, pagination.count());
        Page<InquiryEntity> inquiryEntityPage;

        if (pagination.sort().equals(InquirySort.S)) {
            inquiryEntityPage = inquiryRepository.findAllByUserAndIsSolvedIsTrueOrderByCreatedTimeDesc(user, pageable);
        } else if (pagination.sort().equals(InquirySort.N)) {
            inquiryEntityPage = inquiryRepository.findAllByUserAndIsSolvedIsFalseOrderByCreatedTimeDesc(user, pageable);
        } else {
            inquiryEntityPage = inquiryRepository.findAllByUserOrderByCreatedTimeDesc(user, pageable);
        }

        List<InquiryEntity> inquiryEntities = inquiryEntityPage.getContent();
        List<InquiryDto> dtos = inquiryEntities.stream()
                .map(InquiryMapper.INSTANCE::toInquiryDto)
                .toList();

        return new ResponseInquiryDto(dtos, inquiryEntityPage.getTotalElements());
    }

    public ResponseInquiryDetailDto getInquiry(long inquiryId, UserEntity user) {
        InquiryEntity inquiry = inquiryRepository.findByInquiryId(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
        if (!inquiry.getUser().getUserId().equals(user.getUserId())) {
            throw new CustomException(ErrorCode.MISMATCH_INQUIRY_OWNER);
        }

        if (inquiry.getIsSolved()) {
            AnswerEntity answer = answerRepository.findByInquiry(inquiry)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

            return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry, answer);
        }

        return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry);
    }

    public long addInquiry(RequestInquiryDto dto, UserEntity user) {
        InquiryEntity inquiry = InquiryEntity.create(user, dto.title(), dto.content());
        InquiryEntity response = inquiryRepository.save(inquiry);
        return response.getInquiryId();
    }
}

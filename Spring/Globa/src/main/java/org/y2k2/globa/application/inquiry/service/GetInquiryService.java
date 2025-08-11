package org.y2k2.globa.application.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDetailDto;
import org.y2k2.globa.application.inquiry.mapper.InquiryMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

@Service
@RequiredArgsConstructor
public class GetInquiryService {
    private final InquiryRepository inquiryRepository;
    private final AnswerRepository answerRepository;

    public ResponseInquiryDetailDto get(Long inquiryId, Long userId) {
        InquiryEntity inquiry = inquiryRepository.getInquiry(inquiryId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_INQUIRY));
        if (!inquiry.getUser().getUserId().equals(userId)) {
            throw new CustomException(ErrorCode.MISMATCH_INQUIRY_OWNER);
        }

        if (inquiry.getIsSolved()) {
            AnswerEntity answer = answerRepository.getAnswerByInquiryId(inquiryId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ANSWER));

            return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry, answer);
        }

        return InquiryMapper.INSTANCE.toResponseInquiryDetailDto(inquiry);
    }
}

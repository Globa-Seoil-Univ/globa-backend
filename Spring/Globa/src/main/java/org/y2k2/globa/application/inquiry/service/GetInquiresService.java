package org.y2k2.globa.application.inquiry.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.inquiry.dto.common.InquiryDto;
import org.y2k2.globa.application.inquiry.dto.request.InquiryPaginationDto;
import org.y2k2.globa.application.inquiry.dto.response.ResponseInquiryDto;
import org.y2k2.globa.application.inquiry.mapper.InquiryMapper;
import org.y2k2.globa.common.type.InquirySort;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetInquiresService {
    private final InquiryRepository inquiryRepository;

    public ResponseInquiryDto get(InquiryPaginationDto pagination, Long userId) {
        Pageable pageable = PageRequest.of(pagination.page() - 1, pagination.count());
        Page<InquiryEntity> inquires;

        if (pagination.sort().equals(InquirySort.S)) {
            inquires = inquiryRepository.getSolvedInquiries(userId, pageable);
        } else if (pagination.sort().equals(InquirySort.N)) {
            inquires = inquiryRepository.getUnsolvedInquiries(userId, pageable);
        } else {
            inquires = inquiryRepository.getInquiries(userId, pageable);
        }

        List<InquiryDto> response = inquires.getContent().stream()
                .map(InquiryMapper.INSTANCE::toInquiryDto)
                .toList();

        return new ResponseInquiryDto(response, inquires.getTotalElements());
    }
}

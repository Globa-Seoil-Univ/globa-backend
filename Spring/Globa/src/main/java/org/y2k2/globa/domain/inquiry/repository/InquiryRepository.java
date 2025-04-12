package org.y2k2.globa.domain.inquiry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface InquiryRepository {
    InquiryEntity save(InquiryEntity entity);

    Page<InquiryEntity> getInquiries(Long userId, Pageable pageable);
    Page<InquiryEntity> getSolvedInquiries(Long userId, Pageable pageable);
    Page<InquiryEntity> getUnsolvedInquiries(Long userId, Pageable pageable);

    Optional<InquiryEntity> getInquiry(Long inquiryId);
}

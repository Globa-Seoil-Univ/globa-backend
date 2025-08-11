package org.y2k2.globa.infrastructure.persistence.inquiry.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.inquiry.repository.InquiryRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class InquiryRepositoryImpl implements InquiryRepository {
    private final InquiryJpaRepository inquiryJpaRepository;


    @Override
    public InquiryEntity save(InquiryEntity entity) {
        return inquiryJpaRepository.save(entity);
    }

    @Override
    public Page<InquiryEntity> getInquiries(Long userId, Pageable pageable) {
        return inquiryJpaRepository.findAllByUser_UserIdOrderByInquiryIdDesc(userId, pageable);
    }

    @Override
    public Page<InquiryEntity> getSolvedInquiries(Long userId, Pageable pageable) {
        return inquiryJpaRepository.findAllByUser_UserIdAndIsSolvedIsTrueOrderByInquiryIdDesc(userId, pageable);
    }

    @Override
    public Page<InquiryEntity> getUnsolvedInquiries(Long userId, Pageable pageable) {
        return inquiryJpaRepository.findAllByUser_UserIdAndIsSolvedIsFalseOrderByInquiryIdDesc(userId, pageable);
    }

    @Override
    public Optional<InquiryEntity> getInquiry(Long inquiryId) {
        return inquiryJpaRepository.findByInquiryId(inquiryId);
    }
}

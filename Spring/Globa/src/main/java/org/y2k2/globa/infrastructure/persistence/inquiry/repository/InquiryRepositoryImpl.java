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
    public Page<InquiryEntity> getInquiries(UserEntity user, Pageable pageable) {
        return inquiryJpaRepository.findAllByUserOrderByInquiryIdDesc(user, pageable);
    }

    @Override
    public Page<InquiryEntity> getSolvedInquiries(UserEntity user, Pageable pageable) {
        return inquiryJpaRepository.findAllByUserAndIsSolvedIsTrueOrderByInquiryIdDesc(user, pageable);
    }

    @Override
    public Page<InquiryEntity> getUnsolvedInquiries(UserEntity user, Pageable pageable) {
        return inquiryJpaRepository.findAllByUserAndIsSolvedIsFalseOrderByInquiryIdDesc(user, pageable);
    }

    @Override
    public Optional<InquiryEntity> getInquiry(Long inquiryId) {
        return inquiryJpaRepository.findByInquiryId(inquiryId);
    }
}

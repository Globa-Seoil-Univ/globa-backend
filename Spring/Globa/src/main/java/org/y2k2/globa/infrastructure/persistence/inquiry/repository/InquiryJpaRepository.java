package org.y2k2.globa.infrastructure.persistence.inquiry.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

public interface InquiryJpaRepository extends JpaRepository<InquiryEntity, Long> {
    Optional<InquiryEntity> findByInquiryId(Long inquiryId);
    Page<InquiryEntity> findAllByUserOrderByInquiryIdDesc(UserEntity user, Pageable pageable);
    Page<InquiryEntity> findAllByUserAndIsSolvedIsTrueOrderByInquiryIdDesc(UserEntity user, Pageable pageable);
    Page<InquiryEntity> findAllByUserAndIsSolvedIsFalseOrderByInquiryIdDesc(UserEntity user, Pageable pageable);
}

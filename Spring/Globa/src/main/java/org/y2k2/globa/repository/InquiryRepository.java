package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.InquiryEntity;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    InquiryEntity findByInquiryId(long inquiryId);
    Page<InquiryEntity> findAllByOrderByCreatedTimeDesc(Pageable pageable);
    Page<InquiryEntity> findAllBySolvedIsTrueOrderByCreatedTimeDesc(Pageable pageable);
    Page<InquiryEntity> findAllBySolvedIsFalseOrderByCreatedTimeDesc(Pageable pageable);
}

package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.InquiryEntity;

public interface InquiryRepository extends JpaRepository<InquiryEntity, Long> {
    InquiryEntity findByInquiryId(long inquiryId);
}

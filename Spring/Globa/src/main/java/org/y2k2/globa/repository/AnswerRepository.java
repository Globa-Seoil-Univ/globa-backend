package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.AnswerEntity;
import org.y2k2.globa.entity.InquiryEntity;

public interface AnswerRepository extends JpaRepository<AnswerEntity, Long> {
    AnswerEntity findByAnswerId(long answerId);
    AnswerEntity findByInquiry(InquiryEntity inquiry);
}

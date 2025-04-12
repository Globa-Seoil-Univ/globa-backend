package org.y2k2.globa.infrastructure.persistence.answer.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

import java.util.Optional;

public interface AnswerJpaRepository extends JpaRepository<AnswerEntity, Long> {
    Optional<AnswerEntity> findByAnswerId(long answerId);
    Optional<AnswerEntity> findByInquiry_InquiryId(Long inquiryId);
}

package org.y2k2.globa.domain.answer.repository;

import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

import java.util.Optional;

public interface AnswerRepository {
    AnswerEntity save(AnswerEntity entity);
    void delete(AnswerEntity entity);

    Optional<AnswerEntity> getAnswerById(Long answerId);
    Optional<AnswerEntity> getAnswerByInquiryId(Long inquiryId);
}

package org.y2k2.globa.infrastructure.persistence.answer.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.answer.repository.AnswerRepository;
import org.y2k2.globa.infrastructure.persistence.answer.entity.AnswerEntity;
import org.y2k2.globa.infrastructure.persistence.inquiry.entity.InquiryEntity;

import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class AnswerRepositoryImpl implements AnswerRepository {
    private final AnswerJpaRepository answerJpaRepository;


    @Override
    public AnswerEntity save(AnswerEntity entity) {
        return answerJpaRepository.save(entity);
    }

    @Override
    public void delete(AnswerEntity entity) {
        answerJpaRepository.delete(entity);
    }

    @Override
    public Optional<AnswerEntity> getAnswerById(Long answerId) {
        return answerJpaRepository.findByAnswerId(answerId);
    }

    @Override
    public Optional<AnswerEntity> getAnswerByInquiryId(Long inquiryId) {
        return answerJpaRepository.findByInquiry_InquiryId(inquiryId);
    }
}

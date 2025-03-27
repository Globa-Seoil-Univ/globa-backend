package org.y2k2.globa.infrastructure.persistence.survey.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.survey.repository.SurveyRepository;
import org.y2k2.globa.infrastructure.persistence.survey.entity.SurveyEntity;

@RequiredArgsConstructor
@Repository
public class SurveyRepositoryImpl implements SurveyRepository {
    private final SurveyJpaRepository surveyJpaRepository;

    @Override
    public void save(SurveyEntity entity) {
        surveyJpaRepository.save(entity);
    }
}

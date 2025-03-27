package org.y2k2.globa.domain.survey.repository;

import org.y2k2.globa.infrastructure.persistence.survey.entity.SurveyEntity;

public interface SurveyRepository {
    void save(SurveyEntity entity);
}

package org.y2k2.globa.infrastructure.persistence.survey.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.survey.entity.SurveyEntity;

public interface SurveyJpaRepository extends JpaRepository<SurveyEntity, Long> {
}

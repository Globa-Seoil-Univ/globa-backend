package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.SurveyEntity;

public interface SurveyRepository extends JpaRepository<SurveyEntity, Long> {
}

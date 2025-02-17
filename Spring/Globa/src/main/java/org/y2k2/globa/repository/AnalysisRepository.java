package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.y2k2.globa.Projection.KeywordProjection;
import org.y2k2.globa.Projection.QuizGradeProjection;
import org.y2k2.globa.entity.AnalysisEntity;
import org.y2k2.globa.entity.SectionEntity;
import org.y2k2.globa.entity.UserEntity;

import java.util.List;

public interface AnalysisRepository extends JpaRepository<AnalysisEntity, Long> {
    List<AnalysisEntity> findALlBySectionIn(List<SectionEntity> sections);

    List<AnalysisEntity> findAllBySection(SectionEntity section);
}

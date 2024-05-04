package org.y2k2.globa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.QuizEntity;
import org.y2k2.globa.entity.RecordEntity;

import java.util.List;

public interface QuizRepository extends JpaRepository<QuizEntity, Long> {

    QuizEntity findQuizEntityByQuizId(Long quizId);
    List<QuizEntity> findAllByRecordRecordId(Long recordId);

}

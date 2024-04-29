package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.DictionaryEntity;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {
    List<DictionaryEntity> findByWordOrEngWordOrderByCreatedTimeAsc(String word, String engWord);
}

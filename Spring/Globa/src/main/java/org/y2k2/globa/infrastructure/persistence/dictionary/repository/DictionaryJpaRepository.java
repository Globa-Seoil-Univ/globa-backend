package org.y2k2.globa.infrastructure.persistence.dictionary.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.util.List;

public interface DictionaryJpaRepository extends JpaRepository<DictionaryEntity, Long> {
    @Transactional
    @Modifying
    @Query(value = "TRUNCATE TABLE dictionary", nativeQuery = true)
    void truncate();

    @Query(
            value = "SELECT d FROM DictionaryEntity d " +
                    "WHERE d.word LIKE CONCAT(:word, '%') " +
                        "OR d.engWord LIKE CONCAT(:engWord, '%') " +
                    "ORDER BY LENGTH(d.word), d.createdTime ASC " +
                    "LIMIT 10 "
    )
    List<DictionaryEntity> findTop10ByWord(String word, String engWord);
}

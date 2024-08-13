package org.y2k2.globa.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.y2k2.globa.entity.DictionaryEntity;

import java.util.List;

public interface DictionaryRepository extends JpaRepository<DictionaryEntity, Long> {
    @Query(
            value = "SELECT * FROM dictionary " +
                    "WHERE word LIKE CONCAT(:word, '%') " +
                    "OR eng_word LIKE CONCAT(:engWord, '%') " +
                    "ORDER BY LENGTH(word), created_time ASC " +
                    "LIMIT 10",
            nativeQuery = true
    )
    List<DictionaryEntity> findTop10ByWordStartingWithOrEngWordStartingWithOrderByCreatedTimeAsc(String word, String engWord);

    void deleteAllBy();
}

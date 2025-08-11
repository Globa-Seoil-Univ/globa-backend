package org.y2k2.globa.infrastructure.persistence.dictionary.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.domain.dictionary.repository.DictionaryRepository;
import org.y2k2.globa.infrastructure.persistence.dictionary.entity.DictionaryEntity;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RequiredArgsConstructor
@Repository
public class DictionaryRepositoryImpl implements DictionaryRepository {
    private final DictionaryJpaRepository dictionaryJpaRepository;
    private final JdbcTemplate jdbcTemplate;

    @Override
    public void truncate() {
        dictionaryJpaRepository.truncate();
    }

    @Override
    public void bulkInsert(List<DictionaryDto> dtos) {
        final long[] num = {1};

        // bulk insert
        jdbcTemplate.batchUpdate("INSERT INTO dictionary(dictionary_id, word, eng_word, description, category, pronunciation)" +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, num[0]);
                        ps.setString(2, dtos.get(i).word());
                        ps.setString(3, dtos.get(i).engWord());
                        ps.setString(4, dtos.get(i).description());
                        ps.setString(5, dtos.get(i).category());
                        ps.setString(6, dtos.get(i).pronunciation());

                        num[0] += 1L;
                    }

                    @Override
                    public int getBatchSize() {
                        return dtos.size();
                    }
                }
        );
    }

    @Override
    public List<DictionaryEntity> getWords(String word) {
        return dictionaryJpaRepository.findTop10ByWord(word, word);
    }
}

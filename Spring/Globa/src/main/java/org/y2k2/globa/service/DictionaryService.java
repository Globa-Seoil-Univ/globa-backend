package org.y2k2.globa.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.DictionaryDto;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.entity.DictionaryEntity;
import org.y2k2.globa.mapper.DictionaryMapper;
import org.y2k2.globa.repository.DictionaryRepository;
import org.y2k2.globa.util.Excel;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final Excel excel;
    private final DictionaryRepository dictionaryRepository;
    private final JdbcTemplate jdbcTemplate;

    @Transactional
    public ResponseDictionaryDto getDictionary(String keyword) {
        List<DictionaryEntity> dtos = dictionaryRepository.findTop10ByWordStartingWithOrEngWordStartingWithOrderByCreatedTimeAsc(keyword, keyword);
        return new ResponseDictionaryDto(dtos.stream()
                .map(DictionaryMapper.INSTANCE::toDictionaryDto)
                .toList());
    }

    @Transactional
    public void saveDictionary() {
        List<DictionaryDto> dtos = excel.getDictionaryDto();

        dictionaryRepository.deleteAllInBatch();

        final long[] num = {1};
        // bulk insert
        jdbcTemplate.batchUpdate("INSERT INTO dictionary(dictionary_id, word, eng_word, description, category, pronunciation)" +
                        "VALUES (?, ?, ?, ?, ?, ?)",
                new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement ps, int i) throws SQLException {
                        ps.setLong(1, num[0]);
                        ps.setString(2, dtos.get(i).getWord());
                        ps.setString(3, dtos.get(i).getEngWord());
                        ps.setString(4, dtos.get(i).getDescription());
                        ps.setString(5, dtos.get(i).getCategory());
                        ps.setString(6, dtos.get(i).getPronunciation());

                        num[0] += 1L;
                    }

                    @Override
                    public int getBatchSize() {
                        return dtos.size();
                    }
                }
        );
    }
}

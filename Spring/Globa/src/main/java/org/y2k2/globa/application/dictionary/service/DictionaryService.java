package org.y2k2.globa.application.dictionary.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.dictionary.dto.common.DictionaryDto;
import org.y2k2.globa.application.dictionary.dto.response.ResponseDictionaryDto;
import org.y2k2.globa.entity.DictionaryEntity;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.application.dictionary.mapper.DictionaryMapper;
import org.y2k2.globa.repository.DictionaryRepository;
import org.y2k2.globa.repository.UserRoleRepository;
import org.y2k2.globa.common.util.Excel;
import org.y2k2.globa.application.userrole.service.UserRoleService;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final Excel excel;
    private final JdbcTemplate jdbcTemplate;

    private final UserRoleService userRoleService;

    private final UserRoleRepository userRoleRepository;
    private final DictionaryRepository dictionaryRepository;

    @Transactional
    public ResponseDictionaryDto getDictionary(String keyword) {
        List<DictionaryEntity> dtos = dictionaryRepository.findTop10ByWordStartingWithOrEngWordStartingWithOrderByCreatedTimeAsc(keyword, keyword);

        return new ResponseDictionaryDto(dtos.stream()
                .map(DictionaryMapper.INSTANCE::toDictionaryDto)
                .toList());
    }

    @Transactional
    public void addDictionary(UserEntity user) {
        Optional<UserRoleEntity> optionalUserRole = userRoleRepository.findByUser(user);

        if (optionalUserRole.isEmpty()) {
            userRoleService.createUserRoleAndThrowException(user);
        } else {
            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_DICTIONARY);
        }

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
}

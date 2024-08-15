package org.y2k2.globa.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.y2k2.globa.dto.DictionaryDto;
import org.y2k2.globa.dto.ResponseDictionaryDto;
import org.y2k2.globa.dto.UserRole;
import org.y2k2.globa.entity.DictionaryEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.mapper.DictionaryMapper;
import org.y2k2.globa.repository.DictionaryRepository;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.repository.UserRoleRepository;
import org.y2k2.globa.util.Excel;
import org.y2k2.globa.util.JwtTokenProvider;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DictionaryService {
    private final Excel excel;
    private final JdbcTemplate jdbcTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final DictionaryRepository dictionaryRepository;

    @Transactional
    public ResponseDictionaryDto getDictionary(String accessToken, String keyword) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity user = userRepository.findByUserId(userId);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        List<DictionaryEntity> dtos = dictionaryRepository.findTop10ByWordStartingWithOrEngWordStartingWithOrderByCreatedTimeAsc(keyword, keyword);
        return new ResponseDictionaryDto(dtos.stream()
                .map(DictionaryMapper.INSTANCE::toDictionaryDto)
                .toList());
    }

    @Transactional
    public void saveDictionary(String accessToken) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity user = userRepository.findByUserId(userId);

        if (user == null) throw new CustomException(ErrorCode.NOT_FOUND_USER);
        if (user.getDeleted()) throw new CustomException(ErrorCode.DELETED_USER);

        UserRoleEntity userRole = userRoleRepository.findByUser(user);
        String roleName = userRole.getRoleId().getName();
        boolean isAdminOrEditor = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_DICTIONARY);

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

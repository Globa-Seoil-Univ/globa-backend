package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.record.dto.response.ResponseRecordSearchDto;
import org.y2k2.globa.application.record.mapper.RecordMapper;
import org.y2k2.globa.application.user.dto.common.UserIntroDto;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;

@Service
@RequiredArgsConstructor
public class SearchRecordService {
    private final RecordRepository recordRepository;

    public ResponseRecordSearchDto search(String keyword, int page, int count, Long userId) {
        PageRequest pageable = PageRequest.of(page - 1, count);
        Page<RecordSearchProjection> records = recordRepository.getRecordByKeyword(userId, keyword, pageable);

        return new ResponseRecordSearchDto(records.stream()
                .map(record -> {
                    UserIntroDto uploader = new UserIntroDto(record.getUserId(), record.getProfilePath(), record.getName());
                    return RecordMapper.INSTANCE.toResponseRecordSearch(record.getFolderId(), record, uploader);
                }).toList(),
                records.getTotalElements());
    }
}

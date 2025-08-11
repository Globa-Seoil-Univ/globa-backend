package org.y2k2.globa.application.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
import org.y2k2.globa.application.notice.mapper.NoticeMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

@Service
@RequiredArgsConstructor
public class GetNoticeDetailService {
    private final NoticeRepository noticeRepository;

    public ResponseNoticeDetailDto get(Long noticeId) {
        NoticeEntity notice = noticeRepository.getNotice(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTICE));

        return NoticeMapper.INSTANCE.toDetailResponseDto(notice);
    }
}

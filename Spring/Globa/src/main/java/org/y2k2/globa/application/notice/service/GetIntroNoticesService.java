package org.y2k2.globa.application.notice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
import org.y2k2.globa.application.notice.mapper.NoticeMapper;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetIntroNoticesService {
    private final NoticeRepository noticeRepository;

    public ResponseNoticeIntroDto get() {
        List<NoticeEntity> notices = noticeRepository.getNotices(Limit.of(3));

        return new ResponseNoticeIntroDto(
                notices.stream()
                        .map(NoticeMapper.INSTANCE::toIntroNoticeDto)
                        .toList()
        );
    }
}

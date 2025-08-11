package org.y2k2.globa.domain.notice.repository;

import org.springframework.data.domain.Limit;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.List;
import java.util.Optional;

public interface NoticeRepository {
    NoticeEntity save(NoticeEntity entity);

    List<NoticeEntity> getNotices(Limit limit);

    Optional<NoticeEntity> getNotice(Long noticeId);
}

package org.y2k2.globa.infrastructure.persistence.notice.repository;

import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.List;
import java.util.Optional;

public interface NoticeJpaRepository extends JpaRepository<NoticeEntity, Long> {
    List<NoticeEntity> findByOrderByNoticeIdDesc(Limit limit);

    Optional<NoticeEntity> findByNoticeId(Long noticeId);
}

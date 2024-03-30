package org.y2k2.globa.repository;

import lombok.NonNull;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.entity.NoticeEntity;

import java.util.List;

public interface NoticeRepository extends JpaRepository<NoticeEntity, Long> {
    @NonNull
    List<NoticeEntity> findByOrderByCreatedTimeDesc(Limit limit);

    NoticeEntity findByNoticeId(Long noticeId);
}

package org.y2k2.globa.infrastructure.persistence.notice.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class NoticeRepositoryImpl implements NoticeRepository {
    private final NoticeJpaRepository noticeJpaRepository;

    @Override
    public NoticeEntity save(NoticeEntity entity) {
        return noticeJpaRepository.save(entity);
    }

    @Override
    public List<NoticeEntity> getNotices(Limit limit) {
        return noticeJpaRepository.findByOrderByNoticeIdDesc(limit);
    }

    @Override
    public Optional<NoticeEntity> getNotice(Long noticeId) {
        return noticeJpaRepository.findByNoticeId(noticeId);
    }
}

package org.y2k2.globa.infrastructure.persistence.noticeimage.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.noticeimage.repository.NoticeImageRepository;
import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;

import java.util.List;

@RequiredArgsConstructor
@Repository
public class NoticeImageRepositoryImpl implements NoticeImageRepository {
    private final NoticeImageJpaRepository noticeImageJpaRepository;

    @Override
    public List<NoticeImageEntity> saveAll(List<NoticeImageEntity> entities) {
        return noticeImageJpaRepository.saveAll(entities);
    }
}

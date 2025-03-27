package org.y2k2.globa.domain.noticeimage.repository;

import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;

import java.util.List;

public interface NoticeImageRepository {
    List<NoticeImageEntity> saveAll(List<NoticeImageEntity> entities);
}

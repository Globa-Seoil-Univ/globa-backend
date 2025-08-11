package org.y2k2.globa.infrastructure.persistence.noticeimage.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;

public interface NoticeImageJpaRepository extends JpaRepository<NoticeImageEntity, Long> { }

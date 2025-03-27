package org.y2k2.globa.domain.record.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;

import java.util.List;
import java.util.Optional;

public interface RecordRepository {
    void save(RecordEntity entity);
    void delete(RecordEntity entity);

    List<Long> getAllRecordId(Long userId);
    List<String> getAllPath(FolderEntity folder);

    Page<RecordEntity> getAccessibleRecord(UserEntity user, Pageable pageable);
    Page<RecordSearchProjection> getRecordByKeyword(UserEntity user, String keyword, Pageable pageable);
    Page<RecordEntity> getInvitedRecord(UserEntity user, Pageable pageable);
    Page<RecordEntity> getOwnedRecord(UserEntity user, Pageable pageable);

    Optional<RecordEntity> getRecord(Long recordId);
}

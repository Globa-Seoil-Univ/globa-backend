package org.y2k2.globa.infrastructure.persistence.record.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.projection.RecordSearchProjection;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class RecordRepositoryImpl implements RecordRepository {
    private final RecordJpaRepository recordJpaRepository;

    @Override
    public RecordEntity save(RecordEntity entity) {
        return recordJpaRepository.save(entity);
    }

    @Override
    public void delete(RecordEntity entity) {
        recordJpaRepository.delete(entity);
    }

    @Override
    public List<Long> getAllRecordId(Long userId) {
        return recordJpaRepository.findAllRecordId(userId);
    }

    @Override
    public List<String> getAllPath(FolderEntity folder) {
        return recordJpaRepository.findAllPaths(folder);
    }

    @Override
    public Page<RecordEntity> getAccessibleRecord(UserEntity user, Pageable pageable) {
        return recordJpaRepository.findAllByAccessibleRecord(user, pageable);
    }

    @Override
    public Page<RecordSearchProjection> getRecordByKeyword(UserEntity user, String keyword, Pageable pageable) {
        return recordJpaRepository.findAllSharedOrOwnedRecordsByKeyword(user, keyword, pageable);
    }

    @Override
    public Page<RecordEntity> getInvitedRecord(UserEntity user, Pageable pageable) {
        return recordJpaRepository.findReceivingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
    }

    @Override
    public Page<RecordEntity> getOwnedRecord(UserEntity user, Pageable pageable) {
        return recordJpaRepository.findSharingRecordsByUserOrderByCreatedTimeDesc(user, pageable);
    }

    @Override
    public Optional<RecordEntity> getRecord(Long recordId) {
        return recordJpaRepository.findById(recordId);
    }
}

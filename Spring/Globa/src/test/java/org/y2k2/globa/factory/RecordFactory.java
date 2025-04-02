package org.y2k2.globa.factory;

import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.keyword.repository.KeywordRepository;
import org.y2k2.globa.domain.record.repository.RecordRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.keyword.entity.KeywordEntity;
import org.y2k2.globa.infrastructure.persistence.keyword.repository.KeywordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.record.repository.RecordRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(RecordRepositoryImpl.class)
@Component
public class RecordFactory extends CreatorFactory<RecordEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private RecordRepository recordRepository;

    private UserEntity user;
    private FolderEntity folder;
    private String size = "100";
    private String path = "path";
    private String title = "title";
    private Boolean share = false;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected RecordEntity create() {
        return new RecordEntity();
    }

    @Override
    protected RecordEntity setDefaultValues(RecordEntity entity) {
        entity.setSize(size);
        entity.setPath(path);
        entity.setUser(user);
        entity.setFolder(folder);
        entity.setTitle(title);
        entity.setIsShare(share);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected RecordEntity saveEntity(RecordEntity entity) {
        if (recordRepository != null) {
            return recordRepository.save(entity);
        } else {
            throw new RuntimeException("RecordRepository is null, entity will not be persisted");
        }
    }
}

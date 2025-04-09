package org.y2k2.globa.factory.folder;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.factory.AbstractFactory;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folder.repository.FolderRepositoryImpl;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@Slf4j
@Getter
@Setter
@Import(FolderRepositoryImpl.class)
@Component
public class FolderFactory extends AbstractFactory<FolderEntity> {
    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    @Autowired
    private FolderRepository folderRepository;

    private String title = "DEFAULT";
    private UserEntity user;
    private LocalDateTime createdTime = new CustomTimestamp().getTimestamp();

    @Override
    protected FolderEntity create() {
        return new FolderEntity();
    }

    @Override
    protected FolderEntity setDefaultValues(FolderEntity entity) {
        entity.setTitle(title);
        entity.setUser(user);
        entity.setCreatedTime(createdTime);
        return entity;
    }

    @Override
    protected FolderEntity saveEntity(FolderEntity entity) {
        if (folderRepository != null) {
            return folderRepository.save(entity);
        } else {
            throw new RuntimeException("FolderRepository is null, entity will not be persisted");
        }
    }
}

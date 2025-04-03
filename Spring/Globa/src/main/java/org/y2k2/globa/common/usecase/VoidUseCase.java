package org.y2k2.globa.common.usecase;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

public interface VoidUseCase<T> {
    FolderEntity execute(T command);
}

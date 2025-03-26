package org.y2k2.globa.application.folder;

import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;

public record SaveDefaultFolderCommand(
        UserEntity user
) {
}

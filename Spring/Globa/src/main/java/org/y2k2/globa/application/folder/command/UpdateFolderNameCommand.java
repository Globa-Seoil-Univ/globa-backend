package org.y2k2.globa.application.folder.command;

import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;

public record UpdateFolderNameCommand(
        FolderEntity folder,
        String title
) {
    public static UpdateFolderNameCommand of(FolderEntity folder, String title) {
        return new UpdateFolderNameCommand(folder, title);
    }
}

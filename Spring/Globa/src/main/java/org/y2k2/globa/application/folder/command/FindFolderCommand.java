package org.y2k2.globa.application.folder.command;

public record FindFolderCommand(
        Long folderId,
        Long ownerId
) {
    public static FindFolderCommand of(Long folderId, Long ownerId) {
        return new FindFolderCommand(folderId, ownerId);
    }
}

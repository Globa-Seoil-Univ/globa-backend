package org.y2k2.globa.application.foldershare.command;

public record VerifyFolderCommand(
        Long userId,
        Long folderId
) {
    public static VerifyFolderCommand of(Long userId, Long folderId) {
        return new VerifyFolderCommand(userId, folderId);
    }
}

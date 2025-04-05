package org.y2k2.globa.application.record.command;

public record FindOwnRecordCommand(
        Long userId,
        Long folderId,
        Long recordId
) {
    public static FindOwnRecordCommand of(Long userId, Long folderId, Long recordId) {
        return new FindOwnRecordCommand(userId, folderId, recordId);
    }
}

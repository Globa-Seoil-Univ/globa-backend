package org.y2k2.globa.application.record.command;

import java.util.List;

public record CleanupRecordCommand(
        List<Long> userIds
) {
    public static CleanupRecordCommand of(List<Long> userIds) {
        return new CleanupRecordCommand(userIds);
    }
}

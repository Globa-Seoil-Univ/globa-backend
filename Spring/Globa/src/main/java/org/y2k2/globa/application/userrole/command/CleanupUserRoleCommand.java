package org.y2k2.globa.application.userrole.command;

import java.util.List;

public record CleanupUserRoleCommand(
        List<Long> userIds
) {
    public static CleanupUserRoleCommand of(List<Long> userIds) {
        return new CleanupUserRoleCommand(userIds);
    }
}

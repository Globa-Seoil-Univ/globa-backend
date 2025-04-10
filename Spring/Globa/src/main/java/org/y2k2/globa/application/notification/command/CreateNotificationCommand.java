package org.y2k2.globa.application.notification.command;

import org.y2k2.globa.application.notification.dto.common.SendMessage;

import java.util.List;

public record CreateNotificationCommand(
        SendMessage sendMessage
) {
    public static CreateNotificationCommand of(SendMessage sendMessage) {
        return new CreateNotificationCommand(sendMessage);
    }
}

package org.y2k2.globa.application.notification.command;

import org.y2k2.globa.application.notification.dto.common.SendMessage;

import java.util.List;

public record CreateNotificationCommand(
        List<? extends SendMessage> sendMessages
) {
    public static CreateNotificationCommand of(List<? extends SendMessage> sendMessages) {
        return new CreateNotificationCommand(sendMessages);
    }
}

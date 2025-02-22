package org.y2k2.globa.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.common.notification.SendMessage;
import org.y2k2.globa.dto.common.notification.RequestNotificationWithTopicDto;
import org.y2k2.globa.util.fcm.MessageUtil;

import java.util.List;

@EnableAsync
@Component
@RequiredArgsConstructor
public class NotificationListener {
    private final MessageUtil messageUtil;

    @Async
    @EventListener
    public void sendFcmMessageToTopic(RequestNotificationWithTopicDto dto) {
        messageUtil.sendFcmMessageToTopic(dto);
    }

    @Async
    @EventListener(
            condition = "!(#sendMessage instanceof T(org.y2k2.globa.dto.common.notification.RequestNotificationWithTopicDto))"
    )
    public void handleNotificationEvent(SendMessage sendMessage) {
        messageUtil.sendFcmMessage(sendMessage);
    }

    @Async
    @EventListener(
            condition = "!#sendMessages.isEmpty() " +
                    "&& !(#sendMessages.get(0) instanceof T(org.y2k2.globa.dto.common.notification.RequestNotificationWithTopicDto))"
    )
    public void handleNotificationEvent(List<? extends SendMessage> sendMessages) {
        messageUtil.sendFcmMessages(sendMessages);
    }
}

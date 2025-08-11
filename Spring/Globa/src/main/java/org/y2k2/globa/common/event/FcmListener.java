package org.y2k2.globa.common.event;


import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.fcm.dto.common.FcmSubscribeEvent;
import org.y2k2.globa.application.fcm.dto.common.FcmUnSubscribeEvent;
import org.y2k2.globa.common.util.fcm.MessageUtil;

@Component
@RequiredArgsConstructor
public class FcmListener {
    private final MessageUtil messageUtil;

    @EventListener
    public void subscribeTopic(FcmSubscribeEvent fcmSubscribeTopic) {
        messageUtil.subscribeTopic(fcmSubscribeTopic.token(), fcmSubscribeTopic.topic());
    }

    @EventListener
    public void unsubscribeTopic(FcmUnSubscribeEvent fcmSubscribeTopic) {
        messageUtil.unsubscribeTopic(fcmSubscribeTopic.token(), fcmSubscribeTopic.topic());
    }
}

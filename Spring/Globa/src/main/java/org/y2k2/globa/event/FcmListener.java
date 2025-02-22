package org.y2k2.globa.event;


import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.common.fcm.FcmSubscribeEvent;
import org.y2k2.globa.dto.common.fcm.FcmUnSubscribeEvent;
import org.y2k2.globa.util.fcm.MessageUtil;

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

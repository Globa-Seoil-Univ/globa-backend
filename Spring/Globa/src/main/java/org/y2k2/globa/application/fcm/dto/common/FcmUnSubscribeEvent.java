package org.y2k2.globa.application.fcm.dto.common;

import lombok.Builder;

@Builder
public record FcmUnSubscribeEvent(
        String token,
        String topic
) implements FcmSubscribeTopic {}

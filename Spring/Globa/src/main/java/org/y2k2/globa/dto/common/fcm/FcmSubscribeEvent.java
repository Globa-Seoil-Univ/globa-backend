package org.y2k2.globa.dto.common.fcm;

import lombok.Builder;

@Builder
public record FcmSubscribeEvent(
        String token,
        String topic
) implements FcmSubscribeTopic {}

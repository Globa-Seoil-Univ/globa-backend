package org.y2k2.globa.application.fcm.dto.request;

import org.y2k2.globa.common.annotation.EnumValue;
import org.y2k2.globa.common.type.FcmTopic;

public record RequestSubscribeTopicDto(
        @EnumValue(enumClass = FcmTopic.class, message = "알림 주제 타입은 NOTICE 또는 EVENT만 가능합니다.")
        String topic
) {}

package org.y2k2.globa.application.kafka.dto.response;

import org.y2k2.globa.application.kafka.dto.common.ErrorInfo;
import org.y2k2.globa.application.kafka.dto.common.ErrorStatus;

public record ResponseDLQDto(
        Long recordId,
        ErrorInfo info,
        ErrorStatus status
) {}

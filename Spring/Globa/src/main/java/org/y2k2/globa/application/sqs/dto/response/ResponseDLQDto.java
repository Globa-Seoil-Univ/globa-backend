package org.y2k2.globa.application.sqs.dto.response;

import org.y2k2.globa.application.sqs.dto.common.ErrorInfo;
import org.y2k2.globa.application.sqs.dto.common.ErrorStatus;

public record ResponseDLQDto(
        Long recordId,
        String userId,
        ErrorInfo errorInfo,
        ErrorStatus errorStatus
) {}

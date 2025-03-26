package org.y2k2.globa.application.fcm.dto.common;

import lombok.Builder;

@Builder
public record FcmData(
        Long recordId,
        Long folderId,
        Long inquiryId
) { }

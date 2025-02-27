package org.y2k2.globa.dto.common.fcm;

import lombok.Builder;

@Builder
public record FcmData(
        Long recordId,
        Long folderId,
        Long inquiryId
) { }

package org.y2k2.globa.application.sqs.dto.common;

import lombok.Builder;
import org.y2k2.globa.infrastructure.persistence.record.entity.RecordEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Builder
public record ConsumerValidateDto(
        Boolean isValidated,
        UserEntity user,
        RecordEntity record
) {
}

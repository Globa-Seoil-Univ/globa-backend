package org.y2k2.globa.dto;

import lombok.*;
import org.y2k2.globa.entity.RecordEntity;
import org.y2k2.globa.entity.UserEntity;

@Getter
@Setter
@NoArgsConstructor
public class ConsumerValidateDto {
    private Boolean isValidated;
    private UserEntity user;
    private RecordEntity record;

    @Builder
    public ConsumerValidateDto(Boolean isValidated, UserEntity user, RecordEntity record) {
        this.isValidated = isValidated;
        this.user = user;
        this.record = record;
    }
}

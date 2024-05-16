package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class KafkaRequestDto {
    private long recordId;
    private long userId;
}

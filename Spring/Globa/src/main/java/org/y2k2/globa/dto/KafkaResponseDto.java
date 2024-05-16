package org.y2k2.globa.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class KafkaResponseDto {
    private long recordId = 0;
    private long userId = 0;
    private String message;
}

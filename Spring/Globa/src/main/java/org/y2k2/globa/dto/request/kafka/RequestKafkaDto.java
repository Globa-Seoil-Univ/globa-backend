package org.y2k2.globa.dto.request.kafka;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RequestKafkaDto {
    private long recordId;
    private long userId;
}

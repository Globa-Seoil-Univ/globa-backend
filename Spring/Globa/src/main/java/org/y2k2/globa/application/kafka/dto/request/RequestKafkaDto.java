package org.y2k2.globa.application.kafka.dto.request;

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

package org.y2k2.globa.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.RequestKafkaDto;

@Slf4j
@Component
@RequiredArgsConstructor
public class KafkaProducer {
    private final KafkaTemplate<String, RequestKafkaDto> kafkaTemplate;

    public void send(String topic, String key, RequestKafkaDto request) {
        log.info("Sending message to topic: " + topic + " with key: " + key + " and value: " + request);
        kafkaTemplate.send(topic, key, request);
    }
}

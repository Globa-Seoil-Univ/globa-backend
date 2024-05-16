package org.y2k2.globa.util;

import lombok.Data;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.y2k2.globa.dto.KafkaResponseDto;
import org.y2k2.globa.service.KafkaService;

@Slf4j
@Data
@Getter
@Component
public class KafkaConsumer {
    private final KafkaService kafkaService;

    @KafkaListener(topics = "response", groupId = "globa_python_group")
    public void listen(ConsumerRecord<String, KafkaResponseDto> record, Acknowledgment acknowledgment) {
        try {
            String key = record.key();
            KafkaResponseDto payload = record.value();

            long recordId = payload.getRecordId();
            long userId = payload.getUserId();

            if (key.equalsIgnoreCase("success")
                    && recordId > 0
                    && userId > 0) {
                kafkaService.success(payload);
            } else {
                kafkaService.failed(payload);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to kafka process message : " + e.getMessage());
        }
    }
}

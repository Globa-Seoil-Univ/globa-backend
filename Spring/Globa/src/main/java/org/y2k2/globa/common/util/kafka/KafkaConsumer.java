package org.y2k2.globa.common.util.kafka;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.kafka.dto.response.ResponseDLQDto;
import org.y2k2.globa.application.kafka.dto.response.ResponseKafkaDto;
import org.y2k2.globa.application.kafka.service.DLQService;
import org.y2k2.globa.application.kafka.service.KafkaService;

@Slf4j
@Data
@Component
public class KafkaConsumer {
    private final KafkaService kafkaService;
    private final DLQService dlqService;

    @KafkaListener(topics = "response", groupId = "globa_audio_group_record", containerFactory = "recordKafkaListenerContainerFactory")
    public void listen(ConsumerRecord<String, ResponseKafkaDto> record, Acknowledgment acknowledgment) {
        try {
            String key = record.key();
            ResponseKafkaDto payload = record.value();

            long recordId = payload.recordId();
            long userId = payload.userId();

            if (key.equalsIgnoreCase("success")
                    && recordId > 0
                    && userId > 0) {
                kafkaService.success(payload);
            } else {
                kafkaService.failed(payload);
            }

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to kafka process message = " + e.getMessage());
        }
    }

    @KafkaListener(topics = "response_dlq", groupId = "globa_audio_group_dlq", containerFactory = "dlqKafkaListenerContainerFactory")
    public void listenDLQ(ConsumerRecord<String, ResponseDLQDto> record, Acknowledgment acknowledgment) {
        try {
            ResponseDLQDto payload = record.value();
            dlqService.process(payload);

            acknowledgment.acknowledge();
        } catch (Exception e) {
            log.error("Failed to kafka process DLQ message = " + e.getMessage());
        }
    }
}

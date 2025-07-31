package org.y2k2.globa.common.config;

import com.fasterxml.jackson.core.JsonParseException;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.kafka.support.serializer.ErrorHandlingDeserializer;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.util.backoff.BackOff;
import org.springframework.util.backoff.FixedBackOff;
import org.y2k2.globa.application.kafka.dto.response.ResponseDLQDto;
import org.y2k2.globa.application.kafka.dto.response.ResponseKafkaDto;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Configuration
@EnableKafka
@Slf4j
public class KafkaConsumerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    @Value("${spring.kafka.consumer.auto-offset-reset}")
    private String autoOffsetReset;
    @Value("${spring.kafka.consumer.enable-auto-commit}")
    private String enableAutoCommit;
    @Value("${spring.kafka.consumer.group-id}")
    private String groupId;
    @Value("${kafka.consumer.interval}")
    private Long interval;
    @Value("${kafka.consumer.max_failure}")
    private Long maxAttempts;

    @Bean
    public ConsumerFactory<String, ResponseKafkaDto> recordConsumerFactory() {
        return createConsumerFactory(ResponseKafkaDto.class, groupId + "_record");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResponseKafkaDto> recordKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ResponseKafkaDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(recordConsumerFactory());
        factory.setCommonErrorHandler(recordErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(1);

        return factory;
    }

    @Bean
    public ConsumerFactory<String, ResponseDLQDto> dlqConsumerFactory() {
        return createConsumerFactory(ResponseDLQDto.class, groupId + "_dlq");
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, ResponseDLQDto> dlqKafkaListenerContainerFactory() {
        ConcurrentKafkaListenerContainerFactory<String, ResponseDLQDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(dlqConsumerFactory());
        factory.setCommonErrorHandler(dlqErrorHandler());
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        factory.setConcurrency(1);

        return factory;
    }

    private <T> ConsumerFactory<String, T> createConsumerFactory(Class<T> targetType, String groupId) {
        Map<String, Object> consumerProperties = new HashMap<>();
        consumerProperties.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        consumerProperties.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProperties.put(ConsumerConfig.CLIENT_ID_CONFIG,
                "globa-audio-client-" + targetType.getSimpleName().toLowerCase() + "-" + UUID.randomUUID());
        consumerProperties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
        consumerProperties.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, enableAutoCommit);
        consumerProperties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
        consumerProperties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);

        // Trusted packages 설정
        consumerProperties.put(JsonDeserializer.TRUSTED_PACKAGES, "org.y2k2.globa.application.kafka.dto.response.*");

        consumerProperties.put(JsonDeserializer.VALUE_DEFAULT_TYPE, targetType.getName());
        consumerProperties.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);

        JsonDeserializer<T> deserializer = new JsonDeserializer<>(targetType, false);
        ErrorHandlingDeserializer<T> errorHandlingDeserializer = new ErrorHandlingDeserializer<>(deserializer);

        return new DefaultKafkaConsumerFactory<>(
                consumerProperties,
                new StringDeserializer(), errorHandlingDeserializer
        );
    }

    @Bean
    public DefaultErrorHandler recordErrorHandler() {
        BackOff fixedBackOff = new FixedBackOff(interval, maxAttempts);

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {
            log.error("Failed to process message: " + consumerRecord.value() + " with error: " + e.getMessage());
        }, fixedBackOff);

        errorHandler.addRetryableExceptions(SocketTimeoutException.class);
        errorHandler.addNotRetryableExceptions(NullPointerException.class);
        errorHandler.addNotRetryableExceptions(JsonParseException.class);
        errorHandler.addNotRetryableExceptions(SerializationException.class);

        return errorHandler;
    }

    @Bean
    public DefaultErrorHandler dlqErrorHandler() {
        BackOff fixedBackOff = new FixedBackOff(0L, 0L); // 재시도 없이 바로 처리

        DefaultErrorHandler errorHandler = new DefaultErrorHandler((consumerRecord, e) -> {
            log.error("Failed to process DLQ message: " + consumerRecord.value() + " with error: " + e.getMessage());
        }, fixedBackOff);

        errorHandler.addRetryableExceptions(SocketTimeoutException.class);
        errorHandler.addNotRetryableExceptions(NullPointerException.class);
        errorHandler.addNotRetryableExceptions(JsonParseException.class);
        errorHandler.addNotRetryableExceptions(SerializationException.class);

        return errorHandler;
    }
}

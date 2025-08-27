package org.y2k2.globa.common.util.sqs;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.sqs.dto.request.RequestSQSDto;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

@Slf4j
@Component
@RequiredArgsConstructor
public class SQSSender {
    private final SqsTemplate sqsTemplate;
    private final ObjectMapper objectMapper;

    @Value("${spring.cloud.aws.sqs.send-queue-name}")
    private String queueName;

    public void sendMessage(RequestSQSDto request) {
        log.info("Sending message to SQS value: " + request);

        try {
            String payload = objectMapper.writeValueAsString(request);
            sqsTemplate.send(queueName, payload);
        } catch (JsonProcessingException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }
    }
}

package org.y2k2.globa.application.sqs.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.sqs.dto.response.ResponseDLQDto;
import org.y2k2.globa.application.sqs.dto.response.ResponseSQSDto;
import org.y2k2.globa.domain.dlq.repository.DLQRepository;
import org.y2k2.globa.infrastructure.persistence.dlq.entity.DLQEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class DLQService {
    private final SQSService sqsService;
    private final DLQRepository dlqRepository;

    @Transactional
    public void process(ResponseDLQDto dto) {
        log.error("DLQ processing = {}", dto);

        if (dto.errorInfo() == null) {
            sqsService.failed(new ResponseSQSDto(dto.recordId(), dto.userId(), "FAILED", "In case of retry exceeded"));
            return;
        }

        DLQEntity entity = new DLQEntity();

        entity.setType(dto.errorInfo().type().name());
        entity.setStep(dto.errorInfo().step().name());
        entity.setMessage(dto.errorInfo().message());
        entity.setOccurrenceTime(dto.errorInfo().timestamp());

        dlqRepository.save(entity);

        sqsService.failed(new ResponseSQSDto(dto.recordId(), dto.userId(), "FAILED", dto.toString()));
    }
}

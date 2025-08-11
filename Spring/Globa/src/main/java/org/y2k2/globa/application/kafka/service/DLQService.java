package org.y2k2.globa.application.kafka.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.kafka.dto.response.ResponseDLQDto;
import org.y2k2.globa.domain.dlq.repository.DLQRepository;
import org.y2k2.globa.infrastructure.persistence.dlq.entity.DLQEntity;

@Slf4j
@Service
@RequiredArgsConstructor
public class DLQService {
    private final DLQRepository dlqRepository;

    @Transactional
    public void process(ResponseDLQDto dto) {
        log.error("DLQ processing = {}", dto);

        DLQEntity entity = new DLQEntity();

        entity.setType(dto.info().type().name());
        entity.setStep(dto.info().step().name());
        entity.setMessage(dto.info().message());
        entity.setOccurrenceTime(dto.info().timestamp());

        dlqRepository.save(entity);
    }
}

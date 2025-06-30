package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.kafka.dto.request.RequestKafkaDto;
import org.y2k2.globa.application.record.command.CreateRecordCommand;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.application.record.usecase.CreateRecordUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.KafkaProducer;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateRecordService {
    @Value("${kafka.topic.audio}")
    private String topic;
    @Value("${kafka.topic.audio.key}")
    private String topicKey;

    private final FindUserUseCase findUserUseCase;
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    private final CreateRecordUseCase createRecordUseCase;

    private final FolderRepository folderRepository;

    private final KafkaProducer kafkaProducer;

    public void create(Long folderId, RequestPostRecordDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);
        FolderEntity folder = folderRepository.getFolder(folderId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

        verifyFolderAccessibleUseCase.execute(VerifyFolderCommand.of(userId, folderId));

        Long createdRecordId = createRecordUseCase.execute(
                CreateRecordCommand.of(
                        folder,
                        user,
                        dto
                )
        );

        kafkaProducer.send(topic, topicKey, new RequestKafkaDto(createdRecordId, user.getUserId(), dto.lang()));
    }
}

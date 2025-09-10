package org.y2k2.globa.application.record.service;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderAccessibleUseCase;
import org.y2k2.globa.application.notification.dto.common.SendMessage;
import org.y2k2.globa.application.record.command.CreateRecordCommand;
import org.y2k2.globa.application.record.dto.request.RequestPostRecordDto;
import org.y2k2.globa.application.record.usecase.CreateRecordUseCase;
import org.y2k2.globa.application.sqs.dto.request.RequestSQSDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.crypto.AESUtil;
import org.y2k2.globa.common.util.sqs.SQSSender;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Service
@RequiredArgsConstructor
public class CreateRecordService {
    private final FindUserUseCase findUserUseCase;
    private final VerifyFolderAccessibleUseCase verifyFolderAccessibleUseCase;
    private final CreateRecordUseCase createRecordUseCase;

    private final FolderRepository folderRepository;

    private final AESUtil aesUtil;
    private final SQSSender sqsSender;

    private final ApplicationEventPublisher eventPublisher;

    public void create(Long folderId, RequestPostRecordDto dto, Long userId) {
        UserEntity sender = null;

        try {
            sender = findUserUseCase.execute(userId);
            FolderEntity folder = folderRepository.getFolder(folderId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_FOLDER));

            verifyFolderAccessibleUseCase.execute(VerifyFolderCommand.of(userId, folderId));

            Long createdRecordId = createRecordUseCase.execute(
                    CreateRecordCommand.of(
                            folder,
                            sender,
                            dto
                    )
            );

            String encryptedUserId = aesUtil.encrypt(sender.getUserId());
            RequestSQSDto request = new RequestSQSDto(createdRecordId, encryptedUserId, dto.lang());
            sqsSender.sendMessage(request);
        } catch (Exception e) {
            if (sender != null) {
                SendMessage message = SendMessage.builder()
                        .sender(sender)
                        .receiver(sender)
                        .title("업로드 실패")
                        .body("파일 업로드에 실패했습니다.")
                        .notificationType(NotificationType.UPLOAD_FAILED)
                        .build();

                eventPublisher.publishEvent(message);
            }

            throw e;
        }
    }
}

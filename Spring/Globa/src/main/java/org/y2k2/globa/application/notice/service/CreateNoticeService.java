package org.y2k2.globa.application.notice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
import org.y2k2.globa.application.notice.mapper.NoticeMapper;
import org.y2k2.globa.application.noticeimage.mapper.NoticeImageMapper;
import org.y2k2.globa.application.notification.command.CreateNotificationCommand;
import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
import org.y2k2.globa.application.notification.usecase.CreateNotificationUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.userrole.usecase.VerifyUserWritableUseCase;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.type.FcmTopic;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
import org.y2k2.globa.domain.notice.repository.NoticeRepository;
import org.y2k2.globa.domain.noticeimage.repository.NoticeImageRepository;
import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;
import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreateNoticeService {
    private final ApplicationEventPublisher publisher;
    private final FileStore fileStore;

    private final FindUserUseCase findUserUseCase;
    private final VerifyUserWritableUseCase verifyUserWritableUseCase;
    private final CreateNotificationUseCase createNotificationUseCase;

    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final DummyImageRepository dummyImageRepository;

    @Transactional
    @FileCleanup
    public Long create(RequestNoticeAddDto dto, Long userId) {
        verifyUserWritableUseCase.execute(userId);

        UserEntity uploader = findUserUseCase.execute(userId);
        FileDto file = fileStore.storeFile("notices/thumbnails/", dto.thumbnail());
        NoticeEntity createdNotice;

        try {
            NoticeEntity notice = NoticeMapper.INSTANCE.toEntity(dto, uploader, file);
            createdNotice = noticeRepository.save(notice);

            if (dto.imageIds() != null) {
                List<DummyImageEntity> dummyImages = dummyImageRepository.getImages(List.of(dto.imageIds()));
                List<NoticeImageEntity> noticeImages = dummyImages.stream().map(
                        dummyImage -> NoticeImageMapper.INSTANCE.toEntity(createdNotice, dummyImage)
                ).toList();

                dummyImageRepository.deleteAll(dummyImages);
                noticeImageRepository.saveAll(noticeImages);
            }
        } catch (Exception e) {
            log.error("Failed to create notice = ", e);
            throw new FileUploadException(file.storePath());
        }

        RequestNotificationWithTopicDto info = RequestNotificationWithTopicDto.builder()
                .sender(uploader)
                .notice(createdNotice)
                .title(createdNotice.getTitle())
                .notificationType(NotificationType.NOTICE)
                .topic(FcmTopic.NOTICE.getTopic())
                .build();

        createNotificationUseCase.execute(CreateNotificationCommand.of(info));
        publisher.publishEvent(info);

        return createdNotice.getNoticeId();
    }
}

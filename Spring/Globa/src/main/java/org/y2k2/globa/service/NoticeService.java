package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.annotation.FileCleanup;
import org.y2k2.globa.dto.common.file.FileDto;
import org.y2k2.globa.dto.request.notice.RequestNoticeAddDto;
import org.y2k2.globa.dto.common.notification.RequestNotificationWithTopicDto;
import org.y2k2.globa.dto.response.notice.ResponseNoticeDetailDto;
import org.y2k2.globa.dto.response.notice.ResponseNoticeIntroDto;
import org.y2k2.globa.mapper.NoticeImageMapper;
import org.y2k2.globa.entity.*;
import org.y2k2.globa.exception.*;
import org.y2k2.globa.repository.*;
import org.y2k2.globa.mapper.NoticeMapper;
import org.y2k2.globa.type.FcmTopic;
import org.y2k2.globa.type.NotificationType;
import org.y2k2.globa.util.file.FileStore;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {
    private final ApplicationEventPublisher publisher;
    private final FileStore fileStore;

    private final UserRoleService userRoleService;
    private final NotificationService notificationService;

    private final UserRoleRepository userRoleRepository;
    private final NoticeRepository noticeRepository;
    private final NoticeImageRepository noticeImageRepository;
    private final DummyImageRepository dummyImageRepository;

    public List<ResponseNoticeIntroDto> getIntroNotices() {
        List<NoticeEntity> noticeEntities = noticeRepository.findByOrderByCreatedTimeDesc(Limit.of(3));

        return noticeEntities.stream()
                .map(NoticeMapper.INSTANCE::toIntroResponseDto)
                .toList();
    }

    public ResponseNoticeDetailDto getNoticeDetail(Long noticeId) {
        NoticeEntity noticeEntity = noticeRepository.findByNoticeId(noticeId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTICE));

        return NoticeMapper.INSTANCE.toDetailResponseDto(noticeEntity);
    }

    @Transactional
    @FileCleanup
    public Long addNotice(RequestNoticeAddDto dto, UserEntity user) {
        Optional<UserRoleEntity> optionalUserRole = userRoleRepository.findByUser(user);

        if (optionalUserRole.isEmpty()) {
            userRoleService.createUserRoleAndThrowException(user);
        } else {
            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
        }

        FileDto fileDto = fileStore.storeFile("notices/thumbnails/", dto.thumbnail());
        NoticeEntity createdNotice;

        try {
            NoticeEntity notice = NoticeMapper.INSTANCE.toEntity(dto, user, fileDto);
            createdNotice = noticeRepository.save(notice);

            if (dto.imageIds() != null) {
                List<DummyImageEntity> dummyImages = dummyImageRepository.findByImageIdIn(dto.imageIds());
                List<NoticeImageEntity> noticeImages = dummyImages.stream().map(
                        dummyImage -> NoticeImageMapper.INSTANCE.toEntity(createdNotice, dummyImage)
                ).toList();

                dummyImageRepository.deleteAllInBatch(dummyImages);
                noticeImageRepository.saveAll(noticeImages);
            }
        } catch (Exception e) {
            log.error("Failed to add notice = ", e);
            throw new FileUploadException(fileDto.storePath());
        }

        RequestNotificationWithTopicDto info = RequestNotificationWithTopicDto.builder()
                .sender(user)
                .notice(createdNotice)
                .title(createdNotice.getTitle())
                .notificationType(NotificationType.NOTICE)
                .topic(FcmTopic.NOTICE.getTopic())
                .build();

        notificationService.saveNotification(info);
        publisher.publishEvent(info);

        return createdNotice.getNoticeId();
    }
}

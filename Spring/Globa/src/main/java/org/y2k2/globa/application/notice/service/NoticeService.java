//package org.y2k2.globa.application.notice.service;
//
//import lombok.RequiredArgsConstructor;
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.data.domain.Limit;
//import org.springframework.stereotype.Service;
//
//import org.springframework.transaction.annotation.Transactional;
//import org.y2k2.globa.common.annotation.FileCleanup;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.common.exception.FileUploadException;
//import org.y2k2.globa.domain.dummyimage.repository.DummyImageRepository;
//import org.y2k2.globa.infrastructure.persistence.dummyimage.entity.DummyImageEntity;
//import org.y2k2.globa.infrastructure.persistence.notice.entity.NoticeEntity;
//import org.y2k2.globa.infrastructure.persistence.notice.repository.NoticeJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.noticeimage.entity.NoticeImageEntity;
//import org.y2k2.globa.infrastructure.persistence.noticeimage.repository.NoticeImageJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.application.common.dto.file.FileDto;
//import org.y2k2.globa.application.notice.dto.request.RequestNoticeAddDto;
//import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
//import org.y2k2.globa.application.notice.dto.response.ResponseNoticeDetailDto;
//import org.y2k2.globa.application.notice.dto.response.ResponseNoticeIntroDto;
//import org.y2k2.globa.application.noticeimage.mapper.NoticeImageMapper;
//import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
//import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
//import org.y2k2.globa.application.notice.mapper.NoticeMapper;
//import org.y2k2.globa.common.type.FcmTopic;
//import org.y2k2.globa.infrastructure.persistence.notification.type.NotificationType;
//import org.y2k2.globa.common.util.file.FileStore;
//import org.y2k2.globa.application.notification.service.NotificationService;
//import org.y2k2.globa.application.userrole.service.UserRoleService;
//
//import java.util.List;
//import java.util.Optional;
//
//@Slf4j
//@Service
//@RequiredArgsConstructor
//@Transactional(readOnly = true)
//public class NoticeService {
//    private final ApplicationEventPublisher publisher;
//    private final FileStore fileStore;
//
//    private final UserRoleService userRoleService;
//    private final NotificationService notificationService;
//
//    private final UserRoleJpaRepository userRoleJpaRepository;
//    private final NoticeJpaRepository noticeJpaRepository;
//    private final NoticeImageJpaRepository noticeImageJpaRepository;
//    private final DummyImageRepository dummyImageRepository;
//
//    public List<ResponseNoticeIntroDto> getIntroNotices() {
//        List<NoticeEntity> noticeEntities = noticeJpaRepository.findByOrderByCreatedTimeDesc(Limit.of(3));
//
//        return noticeEntities.stream()
//                .map(NoticeMapper.INSTANCE::toIntroResponseDto)
//                .toList();
//    }
//
//    public ResponseNoticeDetailDto getNoticeDetail(Long noticeId) {
//        NoticeEntity noticeEntity = noticeJpaRepository.findByNoticeId(noticeId)
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_NOTICE));
//
//        return NoticeMapper.INSTANCE.toDetailResponseDto(noticeEntity);
//    }
//
//    @Transactional
//    @FileCleanup
//    public Long addNotice(RequestNoticeAddDto dto, UserEntity user) {
//        Optional<UserRoleEntity> optionalUserRole = userRoleJpaRepository.findByUser(user);
//
//        if (optionalUserRole.isEmpty()) {
//            userRoleService.createUserRoleAndThrowException(user);
//        } else {
//            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(optionalUserRole.get());
//            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
//        }
//
//        FileDto fileDto = fileStore.storeFile("notices/thumbnails/", dto.thumbnail());
//        NoticeEntity createdNotice;
//
//        try {
//            NoticeEntity notice = NoticeMapper.INSTANCE.toEntity(dto, user, fileDto);
//            createdNotice = noticeJpaRepository.save(notice);
//
//            if (dto.imageIds() != null) {
//                List<DummyImageEntity> dummyImages = dummyImageRepository.findByImageIdIn(dto.imageIds());
//                List<NoticeImageEntity> noticeImages = dummyImages.stream().map(
//                        dummyImage -> NoticeImageMapper.INSTANCE.toEntity(createdNotice, dummyImage)
//                ).toList();
//
//                dummyImageRepository.deleteAllInBatch(dummyImages);
//                noticeImageJpaRepository.saveAll(noticeImages);
//            }
//        } catch (Exception e) {
//            log.error("Failed to add notice = ", e);
//            throw new FileUploadException(fileDto.storePath());
//        }
//
//        RequestNotificationWithTopicDto info = RequestNotificationWithTopicDto.builder()
//                .sender(user)
//                .notice(createdNotice)
//                .title(createdNotice.getTitle())
//                .notificationType(NotificationType.NOTICE)
//                .topic(FcmTopic.NOTICE.getTopic())
//                .build();
//
//        notificationService.saveNotification(info);
//        publisher.publishEvent(info);
//
//        return createdNotice.getNoticeId();
//    }
//}

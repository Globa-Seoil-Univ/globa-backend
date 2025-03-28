//package org.y2k2.globa.application.fcm.service;
//
//import lombok.RequiredArgsConstructor;
//
//import lombok.extern.slf4j.Slf4j;
//import org.springframework.context.ApplicationEventPublisher;
//import org.springframework.stereotype.Service;
//import org.y2k2.globa.application.fcm.dto.common.FcmSubscribeEvent;
//import org.y2k2.globa.application.fcm.dto.common.FcmUnSubscribeEvent;
//import org.y2k2.globa.application.fcm.dto.request.RequestFcmTopicDto;
//import org.y2k2.globa.application.fcm.dto.request.RequestSubscribeTopicDto;
//import org.y2k2.globa.application.notification.dto.common.RequestNotificationWithTopicDto;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
//import org.y2k2.globa.common.type.FcmTopic;
//import org.y2k2.globa.application.userrole.service.UserRoleService;
//
//import java.util.Optional;
//
//@Service
//@Slf4j
//@RequiredArgsConstructor
//public class FcmService {
//    private final ApplicationEventPublisher publisher;
//
//    private final UserRoleService userRoleService;
//
//    private final UserRoleJpaRepository userRoleJpaRepository;
//
//    public void sendTopicNotification(RequestFcmTopicDto dto, UserEntity user) {
//        Optional<UserRoleEntity> userRole = userRoleJpaRepository.findByUser(user);
//
//        if (userRole.isEmpty()) {
//            userRoleService.createUserRoleAndThrowException(user);
//        } else {
//            boolean isAdminOrEditor = userRoleService.isAdminOrEditor(userRole.get());
//            if (!isAdminOrEditor) throw new CustomException(ErrorCode.NOT_DESERVE_FCM);
//        }
//
//        RequestNotificationWithTopicDto info = RequestNotificationWithTopicDto.builder()
//                .title(dto.title())
//                .body(dto.body())
//                .topic(dto.topic())
//                .build();
//
//        publisher.publishEvent(info);
//    }
//
//    public void subscribeTopic(RequestSubscribeTopicDto dto, UserEntity user) {
//        if (dto.topic().equalsIgnoreCase(FcmTopic.NOTICE.getTopic()) && !user.getPrimaryNofi()) {
//            throw new CustomException(ErrorCode.NOT_ALLOW_NOTIFICATION_SETTING);
//        }
//        if (dto.topic().equalsIgnoreCase(FcmTopic.EVENT.getTopic()) && !user.getEventNofi()) {
//            throw new CustomException(ErrorCode.NOT_ALLOW_NOTIFICATION_SETTING);
//        }
//        if (user.getNotificationToken() == null || user.getNotificationToken().isEmpty()) {
//            throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);
//        }
//
//        publisher.publishEvent(
//                FcmSubscribeEvent.builder()
//                        .topic(dto.topic())
//                        .token(user.getNotificationToken())
//                        .build()
//        );
//    }
//
//    public void unsubscribeTopic(RequestSubscribeTopicDto dto, UserEntity user) {
//        if (user.getNotificationToken() == null || user.getNotificationToken().isEmpty()) {
//            throw new CustomException(ErrorCode.NOT_FOUND_NOTIFICATION_TOKEN);
//        }
//
//        publisher.publishEvent(
//                FcmUnSubscribeEvent.builder()
//                        .topic(dto.topic())
//                        .token(user.getNotificationToken())
//                        .build()
//        );
//    }
//}

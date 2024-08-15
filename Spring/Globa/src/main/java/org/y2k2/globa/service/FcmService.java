package org.y2k2.globa.service;

import com.google.firebase.messaging.*;

import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.dto.RequestFcmTopicDto;
import org.y2k2.globa.dto.UserRole;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.repository.UserRoleRepository;
import org.y2k2.globa.util.JwtTokenProvider;

@Service
@Slf4j
@RequiredArgsConstructor
public class FcmService {
    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;

    private final FirebaseMessaging firebaseMessaging;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void sendTopicNotification(String accessToken, RequestFcmTopicDto dto) {
        Long userId = jwtTokenProvider.getUserIdByAccessTokenWithoutCheck(accessToken);
        UserEntity user = userRepository.findByUserId(userId);
        if (user == null) {
            throw new CustomException(ErrorCode.NOT_FOUND_USER);
        }
        if (user.getDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }

        UserRoleEntity userRole = userRoleRepository.findByUser(user);
        if (userRole == null) throw new CustomException(ErrorCode.NOT_NULL_ROLE);

        String roleName = userRole.getRoleId().getName();
        boolean isValid = UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
        if (!isValid) throw new CustomException(ErrorCode.NOT_DESERVE_FCM);


        try {
            Message message = Message.builder()
                    .setNotification(Notification.builder()
                            .setTitle(dto.getTitle())
                            .setBody(dto.getBody())
                            .build())
                    .setTopic(dto.getTopic())
                    .build();

            firebaseMessaging.send(message, false);
        }  catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_FCM_SEND);
        }
    }
}

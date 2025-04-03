package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class GetUserNotificationService {
    private final FindUserUseCase findUserUseCase;

    public ResponseNotificationSettingDto getUserNotification(Long userId) {
        UserEntity user = findUserUseCase.execute(userId);

        if (user.getIsDeleted()) {
            throw new CustomException(ErrorCode.DELETED_USER);
        }

        return UserMapper.INSTANCE.toResponseNotificationSettingDto(user);
    }
}

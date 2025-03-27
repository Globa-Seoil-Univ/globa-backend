package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.user.dto.response.ResponseNotificationSettingDto;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;

@RequiredArgsConstructor
@Service
public class GetUserNotificationService {
    private final FindUserUseCase findUserUseCase;

    public ResponseNotificationSettingDto getUserNotification(CustomUserDetails details) {
        return UserMapper.INSTANCE.toResponseNotificationSettingDto(
                findUserUseCase.execute(details.getUserId())
        );
    }
}

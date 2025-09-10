package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestNotificationSettingDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class UpdateUserNotificationService {
    private final FindUserUseCase findUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    public void update(RequestNotificationSettingDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);

        updateUserUseCase.execute(
                UpdateUserCommand.builder()
                        .user(user)
                        .primaryNofi(dto.primaryNofi())
                        .uploadNofi(dto.uploadNofi())
                        .shareNofi(dto.shareNofi())
                        .eventNofi(dto.eventNofi())
                        .build()
        );
    }
}

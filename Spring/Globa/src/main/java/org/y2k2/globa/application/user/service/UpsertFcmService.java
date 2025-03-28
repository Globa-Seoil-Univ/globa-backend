package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.auth.CustomUserDetails;
import org.y2k2.globa.application.fcm.dto.request.RequestNotificationTokenDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.time.LocalDateTime;

@RequiredArgsConstructor
@Service
public class UpsertFcmService {
    private final FindUserUseCase findUserUseCase;

    private final UserRepository userRepository;

    public void upsert(RequestNotificationTokenDto dto, CustomUserDetails details) {
        UserEntity user = findUserUseCase.execute(details.getUserId());
        user.setNotificationToken(dto.token());
        user.setNotificationTokenTime(new CustomTimestamp().getTimestamp());

        userRepository.save(user);
    }
}

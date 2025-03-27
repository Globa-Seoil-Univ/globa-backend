package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Component
public class FindUserUseCase implements UseCase<Long, UserEntity> {
    private final UserRepository userRepository;

    @Cacheable(
            value = "user",
            key = "#userId",
            condition = "#userId != null",
            unless = "#result == null"
    )
    @Override
    public UserEntity execute(Long userId) {
        return userRepository.getUserByUserId(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));
    }
}

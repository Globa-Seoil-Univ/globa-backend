package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.common.util.crypto.HashUtil;
import org.y2k2.globa.domain.user.repository.UserRepository;

import java.util.Optional;

@RequiredArgsConstructor
@Component
public class FindActiveUserIdUseCase implements UseCase<String, Optional<Long>> {
    private final UserRepository userRepository;

    private final HashUtil hashUtil;

    @Override
    public Optional<Long> execute(String snsId) {
        String hashedSnsId = hashUtil.hash(snsId);

        return userRepository.getUserBySnsId(hashedSnsId)
                .map(user -> {
                    if (user.getIsDeleted()) {
                        throw new CustomException(ErrorCode.DELETED_USER);
                    }

                    return user.getUserId();
                });
    }
}

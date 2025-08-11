package org.y2k2.globa.application.userrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;

@Component
@RequiredArgsConstructor
public class VerifyUserWritableUseCase implements VoidUseCase<Long> {
    private final UserRoleRepository userRoleRepository;

    @Override
    public void execute(Long userId) {
        Boolean isWritable = userRoleRepository.isWritable(userId);

        if (!isWritable) {
            throw new CustomException(ErrorCode.NOT_PERMISSION);
        }
    }
}

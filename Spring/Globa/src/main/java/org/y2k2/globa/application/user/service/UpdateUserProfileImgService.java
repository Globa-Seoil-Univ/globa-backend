package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestProfileImageDto;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.common.annotation.FileCleanup;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Service
public class UpdateUserProfileImgService {
    private final FindUserUseCase findUserUseCase;
    private final UpdateUserUseCase updateUserUseCase;

    private final FileStore fileStore;

    @FileCleanup
    @CacheEvict(
            value = "user",
            key = "#userId",
            condition = "#userId != null"
    )
    public void update(RequestProfileImageDto dto, Long userId) {
        UserEntity user = findUserUseCase.execute(userId);

        String oldProfilePath = user.getProfilePath();
        FileDto file = fileStore.storeFile("profiles/", dto.profile());

        try {
            updateUserUseCase.execute(
                    UpdateUserCommand.builder()
                            .user(user)
                            .profileImage(file)
                            .build()
            );
        } catch (Exception e) {
            throw new FileUploadException(file.storePath());
        }

        if (oldProfilePath != null && !oldProfilePath.isEmpty()) {
            fileStore.deleteFile(oldProfilePath);
        }
    }
}

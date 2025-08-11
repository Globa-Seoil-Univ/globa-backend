package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.CleanupUserCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CleanupUserUseCase implements VoidUseCase<CleanupUserCommand> {
    private final UserRepository userRepository;
    private final FileStore fileStore;

    @Override
    public void execute(CleanupUserCommand command) {
        List<String> profilePaths = command.users().stream()
                .map(UserEntity::getProfilePath)
                .toList();

        fileStore.deleteFiles(profilePaths);
        userRepository.deletes(command.users());
    }
}

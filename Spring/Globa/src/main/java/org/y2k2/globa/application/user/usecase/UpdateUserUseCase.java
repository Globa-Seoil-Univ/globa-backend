package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Component
public class UpdateUserUseCase implements VoidUseCase<UpdateUserCommand> {
    private final UserRepository userRepository;

    @Override
    public void execute(UpdateUserCommand command) {
        UserEntity user = command.user();

        if (command.name() != null) {
            user.updateName(command.name());
        }

        if (command.profileImage() != null) {
            user.updateProfile(command.profileImage());
        }

        if (command.uploadNofi() != null && command.shareNofi() != null && command.eventNofi() != null) {
            user.updateNotification(
                    command.uploadNofi(),
                    command.shareNofi(),
                    command.eventNofi()
            );
        }

        userRepository.save(user);
    }
}

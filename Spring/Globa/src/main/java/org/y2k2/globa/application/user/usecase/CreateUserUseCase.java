package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@RequiredArgsConstructor
@Component
public class CreateUserUseCase implements UseCase<CreateUserCommand, UserEntity> {
    private final UserRepository userRepository;

    @Override
    public UserEntity execute(CreateUserCommand command) {
        return userRepository.save(
                UserMapper.INSTANCE.toEntity(command)
        );
    }
}

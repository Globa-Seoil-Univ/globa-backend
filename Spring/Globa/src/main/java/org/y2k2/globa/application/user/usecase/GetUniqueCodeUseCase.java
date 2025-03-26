package org.y2k2.globa.application.user.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.common.usecase.UseCase;
import org.y2k2.globa.domain.user.UserRepository;

@RequiredArgsConstructor
@Component
public class GetUniqueCodeUseCase implements UseCase<CreateUserCommand, String> {
    private final UserRepository userRepository;

    @Override
    public String execute(CreateUserCommand command) {
        String code = command.generateRandomCode();

        while (userRepository.existsByCode(code)) {
            code = command.generateRandomCode();
        }

        return code;
    }
}

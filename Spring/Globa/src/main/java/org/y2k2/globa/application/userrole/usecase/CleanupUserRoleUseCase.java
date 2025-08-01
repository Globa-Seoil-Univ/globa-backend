package org.y2k2.globa.application.userrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.userrole.command.CleanupUserRoleCommand;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;

@Component
@RequiredArgsConstructor
public class CleanupUserRoleUseCase implements VoidUseCase<CleanupUserRoleCommand> {
    private final UserRoleRepository userRoleRepository;

    @Override
    public void execute(CleanupUserRoleCommand command) {
        userRoleRepository.deletes(command.userIds());
    }
}

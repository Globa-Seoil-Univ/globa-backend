package org.y2k2.globa.application.userrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.userrole.command.SaveUserRoleCommand;
import org.y2k2.globa.application.userrole.mapper.UserRoleMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.insfrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.domain.role.RoleRepository;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.repository.UserRoleRepository;

@RequiredArgsConstructor
@Component
public class CreateUserRoleUseCase implements VoidUseCase<SaveUserRoleCommand> {
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public void execute(SaveUserRoleCommand command) {
        RoleEntity role = roleRepository.findByName(command.roleName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE));

        UserRoleEntity userRole = UserRoleMapper.INSTANCE.toEntity(command.user(), role);
        userRoleRepository.save(userRole);
    }
}

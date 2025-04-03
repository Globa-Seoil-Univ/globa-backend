package org.y2k2.globa.application.userrole.usecase;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.userrole.command.CreateUserRoleCommand;
import org.y2k2.globa.application.userrole.mapper.UserRoleMapper;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;
import org.y2k2.globa.domain.role.repository.RoleRepository;
import org.y2k2.globa.domain.userrole.repository.UserRoleRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;

@RequiredArgsConstructor
@Component
public class CreateUserRoleUseCase implements VoidUseCase<CreateUserRoleCommand> {
    private final UserRoleRepository userRoleRepository;
    private final RoleRepository roleRepository;

    @Override
    public void execute(CreateUserRoleCommand command) {
        RoleEntity role = roleRepository.getRole(command.roleName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE));

        UserRoleEntity userRole = UserRoleMapper.INSTANCE.toEntity(command.user(), role);
        userRoleRepository.save(userRole);
    }
}

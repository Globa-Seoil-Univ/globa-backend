package org.y2k2.globa.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.y2k2.globa.entity.RoleEntity;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.entity.UserRoleEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.repository.RoleRepository;
import org.y2k2.globa.repository.UserRoleRepository;
import org.y2k2.globa.type.UserRole;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserRoleService {
    private final RoleRepository roleRepository;
    private final UserRoleRepository userRoleRepository;

    public void createUserRoleAndThrowException(UserEntity user) {
        RoleEntity role = roleRepository.findByName(UserRole.USER.getRoleName())
                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE));

        UserRoleEntity useRole = new UserRoleEntity();
        useRole.setUser(user);
        useRole.setRoleId(role);
        userRoleRepository.save(useRole);

        throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
    }

    public boolean isAdminOrEditor(UserRoleEntity userRole) {
        String roleName = userRole.getRoleId().getName();
        return UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
    }
}

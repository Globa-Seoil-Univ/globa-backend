//package org.y2k2.globa.application.userrole.service;
//
//import lombok.RequiredArgsConstructor;
//import org.springframework.stereotype.Service;
//import org.y2k2.globa.infrastructure.persistence.role.entity.RoleEntity;
//import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;
//import org.y2k2.globa.infrastructure.persistence.userrole.entity.UserRoleEntity;
//import org.y2k2.globa.common.exception.CustomException;
//import org.y2k2.globa.common.exception.ErrorCode;
//import org.y2k2.globa.infrastructure.persistence.role.repository.RoleJpaRepository;
//import org.y2k2.globa.infrastructure.persistence.userrole.repository.UserRoleJpaRepository;
//import org.y2k2.globa.domain.role.type.UserRole;
//
//@Service
//@RequiredArgsConstructor
//public class UserRoleService {
//    private final RoleJpaRepository roleJpaRepository;
//    private final UserRoleJpaRepository userRoleJpaRepository;
//
//    public void createUserRoleAndThrowException(UserEntity user) {
//        RoleEntity role = roleJpaRepository.findByName(UserRole.USER.getRoleName())
//                .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_ROLE));
//
//        UserRoleEntity useRole = new UserRoleEntity();
//        useRole.setUser(user);
//        useRole.setRoleId(role);
//        userRoleJpaRepository.save(useRole);
//
//        throw new CustomException(ErrorCode.NOT_DESERVE_ADD_NOTICE);
//    }
//
//    // Entity로 빼기
//    public boolean isAdminOrEditor(UserRoleEntity userRole) {
//        String roleName = userRole.getRoleId().getName();
//        return UserRole.ADMIN.getRoleName().equals(roleName) || UserRole.EDITOR.getRoleName().equals(roleName);
//    }
//}

package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folder.SaveDefaultFolderCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.command.SaveUserCommand;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.application.user.usecase.CreateUserUseCase;
import org.y2k2.globa.application.user.usecase.GetUniqueCodeUseCase;
import org.y2k2.globa.application.user.usecase.ValidateGoogleUseCase;
import org.y2k2.globa.application.user.usecase.ValidateKakaoUseCase;
import org.y2k2.globa.application.userrole.command.SaveUserRoleCommand;
import org.y2k2.globa.application.userrole.usecase.CreateUserRoleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.insfrastructure.persistence.user.entity.UserEntity;
import org.y2k2.globa.domain.user.UserRepository;

@RequiredArgsConstructor
@Service
public class CreateUserService {
    private final ValidateGoogleUseCase googleUseCase;
    private final ValidateKakaoUseCase kakaoUseCase;
    private final GetUniqueCodeUseCase getUniqueCodeUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final CreateUserRoleUseCase createUserRoleUseCase;
    private final CreateDefaultFolderUseCase defaultFolderUseCase;

    private final UserRepository userRepository;

    @Transactional
    public JWT createUser(CreateUserCommand command) {
        if (command.isGoogle()) {
            googleUseCase.execute(command);
        } else if (command.isKakao()) {
            kakaoUseCase.execute(command);
        } else {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        UserEntity user = userRepository.findBySnsId(command.snsId())
                .orElseGet(() -> {
                    String uniqueCode = getUniqueCodeUseCase.execute(command);
                    UserEntity newUser = UserMapper.INSTANCE.toEntity(command, uniqueCode);

                    SaveUserRoleCommand roleCommand = new SaveUserRoleCommand(newUser, UserRole.USER.getRoleName());
                    createUserRoleUseCase.execute(roleCommand);

                    SaveDefaultFolderCommand folderCommand = new SaveDefaultFolderCommand(newUser);
                    defaultFolderUseCase.execute(folderCommand);
                    return newUser;
                });

        return createUserUseCase.execute(new SaveUserCommand(user));
    }
}

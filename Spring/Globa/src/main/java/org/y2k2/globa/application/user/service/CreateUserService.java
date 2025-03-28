package org.y2k2.globa.application.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.command.ValidateSnsCommand;
import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.application.user.usecase.*;
import org.y2k2.globa.application.userrole.command.CreateUserRoleCommand;
import org.y2k2.globa.application.userrole.usecase.CreateUserRoleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.type.SnsKind;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.security.SecureRandom;
import java.util.Random;

@RequiredArgsConstructor
@Service
public class CreateUserService {
    private final ValidateGoogleUseCase googleUseCase;
    private final ValidateKakaoUseCase kakaoUseCase;
    private final FindActiveUserIdUseCase findActiveUserIdUseCase;
    private final CreateUserUseCase createUserUseCase;
    private final CreateJWTUseCase createJWTUseCase;
    private final CreateUserRoleUseCase createUserRoleUseCase;
    private final CreateDefaultFolderUseCase defaultFolderUseCase;

    private final UserRepository userRepository;

    @Transactional
    public JWT signupOrLogin(RequestUserPostDTO dto) {
        ValidateSnsCommand validateCommand = ValidateSnsCommand.of(dto.snsId(), dto.token());

        switch (SnsKind.valueOf(dto.snsKind())) {
            case GOOGLE -> googleUseCase.execute(validateCommand);
            case KAKAO -> kakaoUseCase.execute(validateCommand);
            default -> throw new CustomException(ErrorCode.INVALID_SNS_KIND);
        }

        Long userId = findActiveUserIdUseCase.execute(dto.snsId())
                .orElseGet(() -> {
                    String uniqueCode = getUniqueCode();
                    return createNewUser(dto, uniqueCode);
                });

        return createJWTUseCase.execute(CreateJWTCommand.of(userId));
    }

    private Long createNewUser(RequestUserPostDTO dto, String uniqueCode) {
        UserEntity newUser = createUserUseCase.execute(
                CreateUserCommand.from(dto, uniqueCode)
        );

        createUserRoleUseCase.execute(CreateUserRoleCommand.of(newUser, UserRole.USER.name()));
        defaultFolderUseCase.execute(CreateDefaultFolderCommand.of(newUser));

        return newUser.getUserId();
    }

    public String getUniqueCode() {
        String code = generateRandomCode();

        while (userRepository.isCodeExists(code)) {
            code = generateRandomCode();
        }

        return code;
    }

    private String generateRandomCode(){
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        Random random = new SecureRandom();
        StringBuilder code = new StringBuilder();

        for(int i = 0; i < 6; ++i ){
            int index = random.nextInt(characters.length());
            code.append(characters.charAt(index));
        }

        return code.toString();
    }
}

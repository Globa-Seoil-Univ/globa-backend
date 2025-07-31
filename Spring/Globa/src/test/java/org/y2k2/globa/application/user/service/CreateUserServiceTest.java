package org.y2k2.globa.application.user.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.user.command.CreateJWTCommand;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.application.user.dto.request.RequestUserPostDTO;
import org.y2k2.globa.application.user.usecase.*;
import org.y2k2.globa.application.userrole.command.CreateUserRoleCommand;
import org.y2k2.globa.application.userrole.usecase.CreateUserRoleUseCase;
import org.y2k2.globa.common.util.hash.HashUtil;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.common.util.jwt.JWT;
import org.y2k2.globa.domain.role.type.UserRole;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateUserServiceTest {
    @InjectMocks
    private CreateUserService createUserService;

    @Mock
    private VerifyGoogleUseCase verifyGoogleUseCase;
    @Mock
    private VerifyKakaoUseCase verifyKakaoUseCase;
    @Mock
    private FindActiveUserIdUseCase findActiveUserIdUseCase;
    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private CreateUserUseCase createUserUseCase;
    @Mock
    private CreateJWTUseCase createJWTUseCase;
    @Mock
    private CreateUserRoleUseCase createUserRoleUseCase;
    @Mock
    private CreateDefaultFolderUseCase createDefaultFolderUseCase;
    @Mock
    private UserRepository userRepository;
    @Mock
    private HashUtil hashUtil;

    private final JWT jwt = JWT.builder()
            .grantType("Bearer")
            .accessToken("access_token")
            .accessTokenExpireTime(new CustomTimestamp().getTimestamp())
            .refreshToken("refresh_token")
            .refreshTokenExpireTime(new CustomTimestamp().getTimestamp())
            .build();

    @Test
    @DisplayName("로그인 - 성공 (카카오)")
    void loginKakao() {
        Long userId = 1L;
        RequestUserPostDTO request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestUserPostDTO.class)
                .set("snsKind", "KAKAO")
                .set("snsId", "ASDASD")
                .set("name", "test_name")
                .set("token", "fcm_token")
                .sample();

        VerifySnsCommand validateCommand = VerifySnsCommand.of(request.snsId(), request.token());

        Mockito.doNothing()
                .when(verifyKakaoUseCase)
                .execute(validateCommand);

        Mockito.when(findActiveUserIdUseCase.execute(request.snsId()))
                .thenReturn(Optional.of(userId));

        Mockito.when(createJWTUseCase.execute(CreateJWTCommand.of(userId)))
                .thenReturn(jwt);

        JWT response = createUserService.signupOrLogin(request);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getGrantType()).isEqualTo(jwt.getGrantType());
        Assertions.assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getAccessTokenExpireTime()).isNotNull();
        Assertions.assertThat(response.getRefreshTokenExpireTime()).isNotNull();
    }
    
    @Test
    @DisplayName("로그인 - 성공 (구글)")
    void loginGoogle() {
        Long userId = 1L;
        RequestUserPostDTO request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestUserPostDTO.class)
                .set("snsKind", "GOOGLE")
                .set("snsId", "ASDASD")
                .set("name", "test_name")
                .set("token", "fcm_token")
                .sample();

        VerifySnsCommand validateCommand = VerifySnsCommand.of(request.snsId(), request.token());

        Mockito.doNothing()
                .when(verifyGoogleUseCase)
                .execute(validateCommand);

        Mockito.when(findActiveUserIdUseCase.execute(request.snsId()))
                .thenReturn(Optional.of(userId));

        Mockito.when(createJWTUseCase.execute(CreateJWTCommand.of(userId)))
                .thenReturn(jwt);

        JWT response = createUserService.signupOrLogin(request);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getGrantType()).isEqualTo(jwt.getGrantType());
        Assertions.assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getAccessTokenExpireTime()).isNotNull();
        Assertions.assertThat(response.getRefreshTokenExpireTime()).isNotNull();

        Mockito.verify(userRepository, Mockito.times(0)).isCodeExists(ArgumentMatchers.anyString());
        Mockito.verify(createUserUseCase, Mockito.times(0)).execute(ArgumentMatchers.any(CreateUserCommand.class));
        Mockito.verify(createUserRoleUseCase, Mockito.times(0)).execute(ArgumentMatchers.any(CreateUserRoleCommand.class));
        Mockito.verify(createDefaultFolderUseCase, Mockito.times(0)).execute(ArgumentMatchers.any(CreateDefaultFolderCommand.class));
    }

    @Test
    @DisplayName("회원가입 - 성공")
    void signup() {
        Long userId = 1L;
        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", false)
                .sample();
        RequestUserPostDTO request = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestUserPostDTO.class)
                .set("snsKind", "KAKAO")
                .set("snsId", "ASDASD")
                .set("name", "test_name")
                .set("token", "fcm_token")
                .sample();

        VerifySnsCommand validateCommand = VerifySnsCommand.of(request.snsId(), request.token());

        Mockito.doNothing()
                .when(verifyKakaoUseCase)
                .execute(validateCommand);

        Mockito.when(findActiveUserIdUseCase.execute(request.snsId()))
                .thenReturn(Optional.empty());

        Mockito.when(userRepository.isCodeExists(Mockito.anyString()))
                .thenReturn(false);

        Mockito.when(createUserUseCase.execute(ArgumentMatchers.any(CreateUserCommand.class)))
                .thenReturn(user);

        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.any(FolderRoleCommand.class)))
                .thenReturn(Optional.of(folderRole));

        Mockito.doNothing()
                .when(createUserRoleUseCase)
                .execute(CreateUserRoleCommand.of(user, UserRole.USER));

        Mockito.when(createDefaultFolderUseCase.execute(CreateDefaultFolderCommand.of(folderRole, user)))
                .thenReturn(null);

        Mockito.when(createJWTUseCase.execute(ArgumentMatchers.any(CreateJWTCommand.class)))
                .thenReturn(jwt);

        JWT response = createUserService.signupOrLogin(request);

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getGrantType()).isEqualTo(jwt.getGrantType());
        Assertions.assertThat(response.getAccessToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getRefreshToken()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getAccessTokenExpireTime()).isNotNull();
        Assertions.assertThat(response.getRefreshTokenExpireTime()).isNotNull();
    }
}

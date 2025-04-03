package org.y2k2.globa.application.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.application.user.mapper.UserMapper;
import org.y2k2.globa.application.user.usecase.CreateUserUseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(SpringExtension.class)
public class CreateUserUseCaseTest {
    private CreateUserUseCase createUserUseCase;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        createUserUseCase = new CreateUserUseCase(userRepository);
    }

    @Test
    @DisplayName("유저 생성 - 성공")
    void createUserTest() {
        CreateUserCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(CreateUserCommand.class);

        UserEntity user = UserMapper.INSTANCE.toEntity(command);

        Mockito.when(userRepository.save(Mockito.any(UserEntity.class)))
                .thenReturn(user);

        UserEntity createdUser = createUserUseCase.execute(command);

        Assertions.assertThat(createdUser)
                .as("유저 생성 성공 시 반환된 값이 null이면 안 됩니다.")
                .isEqualTo(user)
                .as("유저 생성 성공 시 Command 객체 값과 다르면 안 됩니다.")
                .satisfies(u -> {
                    Assertions.assertThat(u.getCode())
                            .isEqualTo(command.code());
                    Assertions.assertThat(u.getName())
                            .isEqualTo(command.name());
                    Assertions.assertThat(u.getSnsKind())
                            .isEqualTo(command.snsKind());
                    Assertions.assertThat(u.getSnsId())
                            .isEqualTo(command.snsId());
                    Assertions.assertThat(u.getProfilePath())
                            .isEqualTo(command.profile());
                    Assertions.assertThat(u.getPrimaryNofi())
                            .isEqualTo(command.notification());
                    Assertions.assertThat(u.getUploadNofi())
                            .isEqualTo(command.notification());
                    Assertions.assertThat(u.getShareNofi())
                            .isEqualTo(command.notification());
                    Assertions.assertThat(u.getEventNofi())
                            .isEqualTo(command.eventNotification());
                });

        Mockito.verify(userRepository, Mockito.times(1)).save(Mockito.any(UserEntity.class));
    }
}

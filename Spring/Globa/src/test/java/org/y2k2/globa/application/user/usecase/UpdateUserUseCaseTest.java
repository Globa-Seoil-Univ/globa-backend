package org.y2k2.globa.application.user.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateUserUseCaseTest {
    @InjectMocks
    private UpdateUserUseCase updateUserUseCase;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("유저 수정 - 성공")
    void updateUserTest() {
        UserEntity user = Mockito.mock(UserEntity.class);

        FileDto file = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FileDto.class);

        UpdateUserCommand command = UpdateUserCommand.builder()
                .user(user)
                .name("name")
                .profileImage(file)
                .uploadNofi(true)
                .shareNofi(false)
                .eventNofi(true)
                .build();

        Mockito.when(userRepository.save(user))
                .thenReturn(user);

        updateUserUseCase.execute(command);

        Mockito.verify(userRepository, Mockito.times(1))
                .save(user);

        Mockito.verify(user, Mockito.times(1))
                .updateName(command.name());

        Mockito.verify(user, Mockito.times(1))
                .updateProfile(command.profileImage());

        Mockito.verify(user, Mockito.times(1))
                .updateNotification(
                        command.uploadNofi(),
                        command.shareNofi(),
                        command.eventNofi()
                );
    }
}

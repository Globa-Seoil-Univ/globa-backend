package org.y2k2.globa.service.user;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import com.navercorp.fixturemonkey.jakarta.validation.plugin.JakartaValidationPlugin;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.y2k2.globa.application.common.dto.file.FileDto;
import org.y2k2.globa.application.user.command.UpdateUserCommand;
import org.y2k2.globa.application.user.dto.request.RequestProfileImageDto;
import org.y2k2.globa.application.user.service.UpdateUserProfileImgService;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.application.user.usecase.UpdateUserUseCase;
import org.y2k2.globa.common.exception.FileUploadException;
import org.y2k2.globa.common.util.file.FileStore;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class UpdateUserProfileImgServiceTest {
    @InjectMocks
    private UpdateUserProfileImgService updateUserProfileImgService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private UpdateUserUseCase updateUserUseCase;
    @Mock
    private FileStore fileStore;

    @Test
    @DisplayName("프로필 이미지 수정 - 성공")
    void updateProfile() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", false)
                .set("profilePath", null)
                .sample();
        FileDto file = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FileDto.class);
        RequestProfileImageDto dto = new RequestProfileImageDto(
                new MockMultipartFile("file", "test.jpg", "image/jpeg", "testData".getBytes())
        );

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(fileStore.storeFile("profiles/", dto.profile()))
                .thenReturn(file);

        Mockito.doNothing()
                .when(updateUserUseCase)
                .execute(Mockito.any(UpdateUserCommand.class));

        updateUserProfileImgService.update(dto, userId);

        Mockito.verify(fileStore, Mockito.times(0))
                .deleteFile(Mockito.anyString());
    }

    @Test
    @DisplayName("프로필 이미지 수정 - 성공 (기존 이미지 삭제)")
    void updateProfileAndDeleteOldProfile() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", false)
                .set("profilePath", "testPath")
                .sample();
        FileDto file = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FileDto.class);
        RequestProfileImageDto dto = new RequestProfileImageDto(
                new MockMultipartFile("file", "test.jpg", "image/jpeg", "testData".getBytes())
        );

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(fileStore.storeFile("profiles/", dto.profile()))
                .thenReturn(file);

        Mockito.doNothing()
                .when(updateUserUseCase)
                .execute(Mockito.any(UpdateUserCommand.class));

        Mockito.doNothing()
                .when(fileStore)
                .deleteFile(Mockito.anyString());

        updateUserProfileImgService.update(dto, userId);

        Mockito.verify(fileStore, Mockito.times(1))
                .deleteFile(Mockito.anyString());
    }

    @Test
    @DisplayName("프로필 이미지 수정 - 실패 (db 오류)")
    void updateProfileFail() {
        Long userId = 1L;
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", userId)
                .set("isDeleted", false)
                .set("profilePath", "testPath")
                .sample();
        FileDto file = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(FileDto.class);
        RequestProfileImageDto dto = new RequestProfileImageDto(
                new MockMultipartFile("file", "test.jpg", "image/jpeg", "testData".getBytes())
        );

        Mockito.when(findUserUseCase.execute(userId))
                .thenReturn(user);

        Mockito.when(fileStore.storeFile("profiles/", dto.profile()))
                .thenReturn(file);

        Mockito.doThrow(new FileUploadException(file.storePath()))
                .when(updateUserUseCase)
                .execute(Mockito.any(UpdateUserCommand.class));

        Assertions.assertThatThrownBy(
                () -> updateUserProfileImgService.update(dto, userId)
        )
                .isInstanceOf(FileUploadException.class)
                .hasFieldOrPropertyWithValue("filePath", file.storePath());

        Mockito.verify(fileStore, Mockito.times(0))
                .deleteFile(Mockito.anyString());
    }
}

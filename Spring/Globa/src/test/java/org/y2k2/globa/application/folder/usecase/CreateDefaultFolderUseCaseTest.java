package org.y2k2.globa.application.folder.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.application.user.command.CreateUserCommand;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.fixture.folderrole.FolderRoleFixture;
import org.y2k2.globa.fixture.user.UserFixture;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

@ExtendWith(MockitoExtension.class)
public class CreateDefaultFolderUseCaseTest {
    @InjectMocks
    private CreateDefaultFolderUseCase createDefaultFolderUseCase;

    @Mock
    private FolderRepository folderRepository;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("기본 폴더 생성 - 성공")
    void createDefaultFolderTest() {
        UserEntity user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleId", 1L)
                .set("roleName", FolderRole.OWNER)
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sample();

        CreateDefaultFolderCommand command = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(CreateDefaultFolderCommand.class)
                .set("folderRole", folderRole)
                .set("user", user)
                .sample();

        FolderEntity folder = FolderMapper.INSTANCE.toEntity(command.user(), command.user().getName());
        FolderShareEntity folderShare = FolderShareMapper.INSTANCE.toEntity(
                folder,
                InvitationStatus.ACCEPT,
                command.folderRole(),
                command.user(),
                command.user()
        );

        Mockito.when(folderRepository.save(Mockito.any(FolderEntity.class)))
                .thenReturn(folder);

        Mockito.when(folderShareRepository.save(Mockito.any(FolderShareEntity.class)))
                .thenReturn(folderShare);

        FolderEntity createdFolder = createDefaultFolderUseCase.execute(command);

        Assertions.assertThat(createdFolder.getFolderId()).isEqualTo(folder.getFolderId());
        Assertions.assertThat(createdFolder)
                .as("기본 폴더 생성 성공 시 Command 객체 값과 다르면 안 됩니다.")
                .satisfies(f -> {
                    Assertions.assertThat(f.getUser().getUserId())
                            .isEqualTo(command.user().getUserId());
                    Assertions.assertThat(f.getTitle())
                            .isEqualTo(command.user().getName());
                });

        Mockito.verify(folderRepository, Mockito.times(1)).save(Mockito.any(FolderEntity.class));
        Mockito.verify(folderShareRepository, Mockito.times(1)).save(Mockito.any(FolderShareEntity.class));
    }
}

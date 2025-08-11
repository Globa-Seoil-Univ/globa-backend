package org.y2k2.globa.application.foldershare.usecase;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.foldershare.command.CreateFolderSharesCommand;
import org.y2k2.globa.application.foldershare.command.TargetFolderShareCommand;
import org.y2k2.globa.application.foldershare.mapper.FolderShareMapper;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateFolderSharesUseCaseTest {
    @InjectMocks
    private CreateFolderSharesUseCase createFolderSharesUseCase;

    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("초대 여러 개 생성 - 성공")
    void createFolderShares() {
        List<TargetFolderShareCommand> targets = FixtureMonkey
                .builder()
                .defaultNotNull(true)
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderRoleEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(UserEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMe(TargetFolderShareCommand.class, 3);

        CreateFolderSharesCommand command = FixtureMonkey
                .builder()
                .defaultNotNull(true)
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(UserEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(CreateFolderSharesCommand.class)
                .set("targets", targets)
                .sample();

        log.info("command = {}", command);

        Mockito.when(folderShareRepository.saveAll(Mockito.anyList()))
                .thenReturn(command.targets().stream()
                        .map(target -> FolderShareMapper.INSTANCE.toEntity(
                                command.folder(),
                                command.status(),
                                target.role(),
                                command.ownerUser(),
                                target.user()
                        )).toList());

        List<FolderShareEntity> response = createFolderSharesUseCase.execute(command);

        log.info("response = {}", response);

        Assertions.assertThat(response).isNotEmpty();
        Assertions.assertThat(response).hasSize(command.targets().size());
        Assertions.assertThat(response)
                .allSatisfy(folderShare -> {
                    Assertions.assertThat(folderShare.getFolder()).isEqualTo(command.folder());
                    Assertions.assertThat(folderShare.getInvitationStatus()).isEqualTo(command.status());
                    Assertions.assertThat(folderShare.getRole()).isIn(command.targets().stream()
                            .map(TargetFolderShareCommand::role).toList());
                    Assertions.assertThat(folderShare.getOwnerUser()).isEqualTo(command.ownerUser());
                    Assertions.assertThat(folderShare.getTargetUser()).isIn(command.targets().stream()
                            .map(TargetFolderShareCommand::user).toList());
                });

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .saveAll(Mockito.anyList());
    }

    @Test
    @DisplayName("초대 여러 개 생성 - 성공 (Target이 없는 경우)")
    void createFolderSharesWithoutTargets() {
        CreateFolderSharesCommand command = FixtureMonkey
                .builder()
                .defaultNotNull(true)
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderRoleEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(FolderEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .pushAssignableTypeArbitraryIntrospector(UserEntity.class, BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeOne(CreateFolderSharesCommand.class);

        log.info("command = {}", command);

        Mockito.when(folderShareRepository.saveAll(Mockito.anyList()))
                .thenReturn(List.of());

        List<FolderShareEntity> response = createFolderSharesUseCase.execute(command);

        log.info("response = {}", response);

        Assertions.assertThat(response).isEmpty();

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .saveAll(Mockito.anyList());
    }
}

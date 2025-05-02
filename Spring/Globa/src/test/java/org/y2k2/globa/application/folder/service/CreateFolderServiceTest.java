package org.y2k2.globa.application.folder.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.y2k2.globa.application.folder.dto.request.RequestFolderPostDto;
import org.y2k2.globa.application.folder.mapper.FolderMapper;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.CreateFolderRoleUseCase;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.foldershare.command.CreateFolderSharesCommand;
import org.y2k2.globa.application.foldershare.usecase.CreateFolderSharesUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.user.repository.UserRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class CreateFolderServiceTest {
    @InjectMocks
    private CreateFolderService createFolderService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private CreateFolderRoleUseCase createFolderRoleUseCase;
    @Mock
    private CreateFolderSharesUseCase createFolderSharesUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private UserRepository userRepository;

    UserEntity user;
    FolderRoleEntity owner;
    FolderRoleEntity editor;
    FolderRoleEntity reader;

    @BeforeEach
    void setup() {
        user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .set("isDeleted", false)
                .sample();

        owner = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();
        editor = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.EDITOR)
                .sample();
        reader = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.READER)
                .sample();
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 초대 X)")
    void createFolder() {
        String title = "Test Folder";
        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
        List<FolderShareEntity> folderShares = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("invitationStatus", InvitationStatus.ACCEPT)
                .set("role", owner)
                .set("ownerUser", user)
                .sampleList(1);

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.eq(new FolderRoleCommand(FolderRole.OWNER))))
                .thenReturn(Optional.of(owner));
        Mockito.when(folderRepository.save(ArgumentMatchers.any(FolderEntity.class)))
                .thenReturn(folder);
        Mockito.when(createFolderSharesUseCase.execute(ArgumentMatchers.any(CreateFolderSharesCommand.class)))
                .thenReturn(folderShares);

        FolderShareEntity createdFolderShare = createFolderService.create(title, user.getUserId());

        Assertions.assertThat(createdFolderShare.getFolder().getFolderId()).isEqualTo(folder.getFolderId());
        Assertions.assertThat(createdFolderShare.getFolder().getTitle()).isEqualTo(title);
        Assertions.assertThat(createdFolderShare.getRole().getRoleName()).isEqualTo(owner.getRoleName());
        Assertions.assertThat(createdFolderShare.getOwnerUser().getUserId()).isEqualTo(user.getUserId());
        Assertions.assertThat(createdFolderShare.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(FolderRoleCommand.class));
        Mockito.verify(folderRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(FolderEntity.class));
        Mockito.verify(createFolderSharesUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(CreateFolderSharesCommand.class));
    }

    @Test
    @DisplayName("폴더 생성 - 성공 (공유 초대 O)")
    void createFolderWithTargets() {
        String title = "Test Folder";
        List<RequestFolderPostDto.ShareTarget> targets = FixtureMonkey.builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(RequestFolderPostDto.ShareTarget.class)
                .set("role", FolderRole.EDITOR.toString())
                .set("code", "ABCABC")
                .sampleList(3);
        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
        UserEntity targetUser = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 2L)
                .set("code", "QWERTY")
                .set("isDeleted", false)
                .sample();
        List<FolderShareEntity> folderShares = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("invitationStatus", InvitationStatus.PENDING)
                .set("role", editor)
                .set("ownerUser", user)
                .set("targetUser", targetUser)
                .sampleList(1);

        Mockito.when(userRepository.getAllUsersByCodes(ArgumentMatchers.anyList()))
                .thenReturn(List.of(targetUser));
        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);

        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.any(FolderRoleCommand.class)))
                .thenAnswer(invocation -> {
                    FolderRoleCommand command = invocation.getArgument(0);
                    return switch (command.folderRole()) {
                        case OWNER -> Optional.of(owner);
                        case EDITOR -> Optional.of(editor);
                        case READER -> Optional.of(reader);
                        default -> null;
                    };
                });

        Mockito.when(folderRepository.save(ArgumentMatchers.any(FolderEntity.class)))
                .thenReturn(folder);
        Mockito.when(createFolderSharesUseCase.execute(ArgumentMatchers.any(CreateFolderSharesCommand.class)))
                .thenReturn(folderShares);

        createFolderService.create(title, targets, user.getUserId());

        Mockito.verify(userRepository, Mockito.times(1))
                .getAllUsersByCodes(ArgumentMatchers.anyList());
        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(findFolderRoleUseCase, Mockito.times(3))
                .execute(ArgumentMatchers.any(FolderRoleCommand.class));
        Mockito.verify(folderRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(FolderEntity.class));
        // 자신 1번 + targetUser 1번
        Mockito.verify(createFolderSharesUseCase, Mockito.times(2))
                .execute(ArgumentMatchers.any(CreateFolderSharesCommand.class));
    }
    
    @Test
    @DisplayName("폴더 생성 - 성공 (Folder Role이 존재하지 않은 경우)")
    void createFolderWithoutRole() {
        String title = "Test Folder";
        FolderEntity folder = FolderMapper.INSTANCE.toEntity(user, title);
        List<FolderShareEntity> folderShares = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder", folder)
                .set("invitationStatus", InvitationStatus.ACCEPT)
                .set("role", owner)
                .set("ownerUser", user)
                .sampleList(1);

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.eq(new FolderRoleCommand(FolderRole.OWNER))))
                .thenReturn(Optional.empty());
        Mockito.when(createFolderRoleUseCase.execute(ArgumentMatchers.any(FolderRoleCommand.class)))
                .thenReturn(owner);
        Mockito.when(folderRepository.save(ArgumentMatchers.any(FolderEntity.class)))
                .thenReturn(folder);
        Mockito.when(createFolderSharesUseCase.execute(ArgumentMatchers.any(CreateFolderSharesCommand.class)))
                .thenReturn(folderShares);

        FolderShareEntity createdFolderShare = createFolderService.create(title, user.getUserId());

        Assertions.assertThat(createdFolderShare.getFolder().getFolderId()).isEqualTo(folder.getFolderId());
        Assertions.assertThat(createdFolderShare.getFolder().getTitle()).isEqualTo(title);
        Assertions.assertThat(createdFolderShare.getRole().getRoleName()).isEqualTo(owner.getRoleName());
        Assertions.assertThat(createdFolderShare.getOwnerUser().getUserId()).isEqualTo(user.getUserId());
        Assertions.assertThat(createdFolderShare.getInvitationStatus()).isEqualTo(InvitationStatus.ACCEPT);

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(FolderRoleCommand.class));
        Mockito.verify(createFolderRoleUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(FolderRoleCommand.class));
        Mockito.verify(folderRepository, Mockito.times(1))
                .save(ArgumentMatchers.any(FolderEntity.class));
        Mockito.verify(createFolderSharesUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(CreateFolderSharesCommand.class));
    }
}

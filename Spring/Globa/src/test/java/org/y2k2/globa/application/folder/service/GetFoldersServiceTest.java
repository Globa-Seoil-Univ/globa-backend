package org.y2k2.globa.application.folder.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.folder.command.CreateDefaultFolderCommand;
import org.y2k2.globa.application.folder.dto.response.ResponseFolderDto;
import org.y2k2.globa.application.folder.usecase.CreateDefaultFolderUseCase;
import org.y2k2.globa.application.folderrole.command.FolderRoleCommand;
import org.y2k2.globa.application.folderrole.usecase.FindFolderRoleUseCase;
import org.y2k2.globa.application.user.usecase.FindUserUseCase;
import org.y2k2.globa.common.util.CustomTimestamp;
import org.y2k2.globa.domain.folder.repository.FolderRepository;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.folder.entity.FolderEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.entity.FolderRoleEntity;
import org.y2k2.globa.infrastructure.persistence.folderrole.type.FolderRole;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.user.entity.UserEntity;

import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

@ExtendWith(MockitoExtension.class)
public class GetFoldersServiceTest {
    @InjectMocks
    private GetFoldersService getFoldersService;

    @Mock
    private FindUserUseCase findUserUseCase;
    @Mock
    private FindFolderRoleUseCase findFolderRoleUseCase;
    @Mock
    private CreateDefaultFolderUseCase createDefaultFolderUseCase;
    @Mock
    private FolderRepository folderRepository;
    @Mock
    private FolderShareRepository folderShareRepository;

    UserEntity user;
    List<Long> folderIds = List.of(1L, 2L, 3L);
    List<String> folderTitles = List.of("Folder 1", "Folder 2", "Folder 3");
    List<FolderShareEntity> folderShares;

    @BeforeEach
    void setup() {
        user = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(UserEntity.class)
                .set("userId", 1L)
                .sample();

        List<FolderEntity> folders = IntStream.range(0, 3)
                .mapToObj(i -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .build()
                        .giveMeBuilder(FolderEntity.class)
                        .set("folderId", folderIds.get(i))
                        .set("title", folderTitles.get(i))
                        .set("createdTime", new CustomTimestamp().getTimestamp())
                        .sample())
                .toList();

        folderShares = IntStream.range(0, 3)
                .mapToObj(i -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .build()
                        .giveMeBuilder(FolderShareEntity.class)
                        .set("folder", folders.get(i))
                        .set("targetUser", user)
                        .sample())
                .toList();
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (첫 페이지, 기본 폴더 O)")
    void getFoldersInDefaultFolder() {
        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();
        FolderEntity defaultFolder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("title", "Default Folder")
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sample();
        // 첫 페이지인 경우 기본 폴더를 포함하기 떄문에 count - 1
        Pageable pageable = PageRequest.of(0, 2);

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.eq(FolderRoleCommand.of(FolderRole.OWNER))))
                .thenReturn(Optional.of(folderRole));
        Mockito.when(folderRepository.getDefaultFolder(user.getUserId()))
                .thenReturn(Optional.of(defaultFolder));
        Mockito.when(folderShareRepository.getInvitationsForFolderExcludingDefault(
                ArgumentMatchers.anyLong(),
                ArgumentMatchers.eq(pageable)
        ))
                // 기본 폴더로 인해 count - 1 돼서 가져옴
                .thenReturn(new PageImpl<>(folderShares.subList(0, 2), pageable, folderShares.size()));

        ResponseFolderDto response = getFoldersService.getFolders(1, 3, user.getUserId());

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getFolders()).isNotNull();
        Assertions.assertThat(response.getFolders().size()).isEqualTo(3);

        Assertions.assertThat(response.getFolders().get(0).getFolderId()).isEqualTo(defaultFolder.getFolderId());
        Assertions.assertThat(response.getFolders().get(0).getTitle()).isEqualTo(defaultFolder.getTitle());

        Assertions.assertThat(response.getFolders().subList(1, response.getFolders().size()))
                        .satisfies(res -> {
                            IntStream.range(0, res.size())
                                    .forEach(i -> {
                                        Assertions.assertThat(res.get(i).getFolderId()).isEqualTo(folderShares.get(i).getFolder().getFolderId());
                                        Assertions.assertThat(res.get(i).getTitle()).isEqualTo(folderShares.get(i).getFolder().getTitle());
                                    });
                        });

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.eq(FolderRoleCommand.of(FolderRole.OWNER)));
        Mockito.verify(folderRepository, Mockito.times(1))
                .getDefaultFolder(user.getUserId());
        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getInvitationsForFolderExcludingDefault(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.eq(pageable)
                );
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (첫 페이지, 기본 폴더 X)")
    void getFoldersInDefaultFolderNotExists() {
        FolderRoleEntity folderRole = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderRoleEntity.class)
                .set("roleName", FolderRole.OWNER)
                .sample();
        FolderEntity defaultFolder = FixtureMonkey.builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(FolderEntity.class)
                .set("folderId", 1L)
                .set("title", "Default Folder")
                .set("createdTime", new CustomTimestamp().getTimestamp())
                .sample();
        // 첫 페이지인 경우 기본 폴더를 포함하기 떄문에 count - 1
        Pageable pageable = PageRequest.of(0, 2);

        Mockito.when(findUserUseCase.execute(user.getUserId()))
                .thenReturn(user);
        Mockito.when(findFolderRoleUseCase.execute(ArgumentMatchers.eq(FolderRoleCommand.of(FolderRole.OWNER))))
                .thenReturn(Optional.of(folderRole));
        Mockito.when(folderRepository.getDefaultFolder(user.getUserId()))
                .thenReturn(Optional.empty());
        Mockito.when(createDefaultFolderUseCase.execute(ArgumentMatchers.any(CreateDefaultFolderCommand.class)))
                .thenReturn(defaultFolder);
        Mockito.when(folderShareRepository.getInvitationsForFolderExcludingDefault(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.eq(pageable)
                ))
                // 기본 폴더로 인해 count - 1 돼서 가져옴
                .thenReturn(new PageImpl<>(folderShares.subList(0, 2), pageable, folderShares.size()));

        ResponseFolderDto response = getFoldersService.getFolders(1, 3, user.getUserId());

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getFolders()).isNotNull();
        Assertions.assertThat(response.getFolders().size()).isEqualTo(3);

        Assertions.assertThat(response.getFolders().get(0).getFolderId()).isEqualTo(defaultFolder.getFolderId());
        Assertions.assertThat(response.getFolders().get(0).getTitle()).isEqualTo(defaultFolder.getTitle());

        Assertions.assertThat(response.getFolders().subList(1, response.getFolders().size()))
                .satisfies(res -> {
                    IntStream.range(0, res.size())
                            .forEach(i -> {
                                Assertions.assertThat(res.get(i).getFolderId()).isEqualTo(folderShares.get(i).getFolder().getFolderId());
                                Assertions.assertThat(res.get(i).getTitle()).isEqualTo(folderShares.get(i).getFolder().getTitle());
                            });
                });

        Mockito.verify(findUserUseCase, Mockito.times(1))
                .execute(user.getUserId());
        Mockito.verify(findFolderRoleUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.eq(FolderRoleCommand.of(FolderRole.OWNER)));
        Mockito.verify(createDefaultFolderUseCase, Mockito.times(1))
                .execute(ArgumentMatchers.any(CreateDefaultFolderCommand.class));
        Mockito.verify(folderRepository, Mockito.times(1))
                .getDefaultFolder(user.getUserId());
        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getInvitationsForFolderExcludingDefault(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.eq(pageable)
                );
    }

    @Test
    @DisplayName("폴더 조회 - 성공 (다른 페이지)")
    void getFolders() {
        Pageable pageable = PageRequest.of(1, 3);

        Mockito.when(folderShareRepository.getInvitationsForFolderExcludingDefault(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.eq(pageable)
                ))
                .thenReturn(new PageImpl<>(folderShares, pageable, folderShares.size()));

        ResponseFolderDto response = getFoldersService.getFolders(2, 3, user.getUserId());

        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getFolders()).isNotNull();
        Assertions.assertThat(response.getFolders().size()).isEqualTo(3);

        Assertions.assertThat(response.getFolders().subList(0, response.getFolders().size()))
                .satisfies(res -> {
                    IntStream.range(0, res.size())
                            .forEach(i -> {
                                Assertions.assertThat(res.get(i).getFolderId()).isEqualTo(folderShares.get(i).getFolder().getFolderId());
                                Assertions.assertThat(res.get(i).getTitle()).isEqualTo(folderShares.get(i).getFolder().getTitle());
                            });
                });

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getInvitationsForFolderExcludingDefault(
                        ArgumentMatchers.anyLong(),
                        ArgumentMatchers.eq(pageable)
                );
    }
}

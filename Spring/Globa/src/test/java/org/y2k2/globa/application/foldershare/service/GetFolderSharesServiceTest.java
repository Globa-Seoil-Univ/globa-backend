package org.y2k2.globa.application.foldershare.service;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.BeanArbitraryIntrospector;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.y2k2.globa.application.foldershare.command.VerifyFolderCommand;
import org.y2k2.globa.application.foldershare.dto.response.ResponseFolderShareUserDto;
import org.y2k2.globa.application.foldershare.usecase.VerifyFolderOwnerUseCase;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@ExtendWith(MockitoExtension.class)
public class GetFolderSharesServiceTest {
    @InjectMocks
    private GetFolderSharesService getFolderSharesService;

    @Mock
    private VerifyFolderOwnerUseCase verifyFolderOwnerUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("초대 목록 조회 - 성공")
    public void getFolderSharesSuccess() {
        Long folderId = 1L;
        Long ownerId = 1L;
        ArrayList<Long> targetIds = new ArrayList<>(Arrays.asList(2L, 3L, 4L));

        List<FolderShareEntity> folderShares = targetIds.stream()
                .map(targetId -> FixtureMonkey.builder()
                        .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                        .defaultNotNull(true)
                        .build()
                        .giveMeBuilder(FolderShareEntity.class)
                        .set("folder.folderId", folderId)
                        .set("ownerUser.userId", ownerId)
                        .set("targetUser.userId", targetId)
                        .sample())
                .toList();
        Pageable pageable = PageRequest.of(0, 10);

        Mockito
                .doNothing()
                .when(verifyFolderOwnerUseCase)
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito
                .when(folderShareRepository.getShareInvitations(folderId, pageable))
                .thenReturn(new PageImpl<>(folderShares, pageable, folderShares.size()));

        ResponseFolderShareUserDto response = getFolderSharesService.get(folderId, 1, 10, ownerId);

        Assertions.assertThat(response.total()).isEqualTo(folderShares.size());
        Assertions.assertThat(response.users()).isNotEmpty();
        Assertions.assertThat(response.users())
                .allSatisfy(user -> {
                    Assertions.assertThat(user.shareId()).isNotNull();
                    Assertions.assertThat(user.roleId()).isNotNull();
                    Assertions.assertThat(user.user())
                            .isNotNull()
                            .extracting("userId")
                            .isIn(targetIds);
                    Assertions.assertThat(user.invitationStatus()).isIn(InvitationStatus.PENDING, InvitationStatus.ACCEPT);
                });

        Mockito.verify(verifyFolderOwnerUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyFolderCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getShareInvitations(folderId, pageable);
    }
}

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
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.application.foldershare.usecase.VerifyInvitationUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.domain.foldershare.repository.FolderShareRepository;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
public class RefuseInvitationServiceTest {
    @InjectMocks
    private RefuseInvitationService refuseInvitationService;

    @Mock
    private VerifyInvitationUseCase verifyInvitationUseCase;
    @Mock
    private FolderShareRepository folderShareRepository;

    @Test
    @DisplayName("초대 거절 - 성공")
    public void refuseInvitationSuccess() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long userId = 1L;

        FolderShareEntity folderShare = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", shareId)
                .set("folder.folderId", folderId)
                .set("targetUser.userId", userId)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, userId))
                .thenReturn(Optional.of(folderShare));

        Mockito
                .doNothing()
                .when(verifyInvitationUseCase)
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito
                .doNothing()
                .when(folderShareRepository)
                .delete(Mockito.any(FolderShareEntity.class));

        refuseInvitationService.refuse(folderId, shareId, userId);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, userId);

        Mockito.verify(verifyInvitationUseCase, Mockito.times(1))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .delete(Mockito.any(FolderShareEntity.class));
    }

    @Test
    @DisplayName("초대 거절 - 실패 (초대 존재 X)")
    public void refuseInvitationNotFoundShare() {
        Long folderId = 1L;
        Long shareId = 1L;
        Long userId = 1L;

        Mockito
                .when(folderShareRepository.getShareInvitation(folderId, userId))
                .thenReturn(Optional.empty());

        Assertions
                .assertThatThrownBy(() -> refuseInvitationService.refuse(folderId, shareId, userId))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_SHARE);

        Mockito.verify(folderShareRepository, Mockito.times(1))
                .getShareInvitation(folderId, userId);

        Mockito.verify(verifyInvitationUseCase, Mockito.times(0))
                .execute(Mockito.any(VerifyInvitationCommand.class));

        Mockito.verify(folderShareRepository, Mockito.times(0))
                .delete(Mockito.any(FolderShareEntity.class));
    }
}

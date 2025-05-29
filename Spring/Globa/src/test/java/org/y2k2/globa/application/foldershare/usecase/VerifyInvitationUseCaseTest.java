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
import org.y2k2.globa.application.foldershare.command.VerifyInvitationCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.infrastructure.persistence.foldershare.entity.FolderShareEntity;
import org.y2k2.globa.infrastructure.persistence.foldershare.type.InvitationStatus;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class VerifyInvitationUseCaseTest {
    @InjectMocks
    private VerifyInvitationUseCase verifyInvitationUseCase;

    @Test
    @DisplayName("초대 검증 - 성공")
    void verifyInvitationSuccess() {
        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        VerifyInvitationCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(VerifyInvitationCommand.class)
                .set("folderShare", folderShareEntity)
                .set("shareId", folderShareEntity.getShareId())
                .set("targetId", folderShareEntity.getTargetUser().getUserId())
                .set("folderId", folderShareEntity.getFolder().getFolderId())
                .sample();

        verifyInvitationUseCase.execute(command);
    }

    @Test
    @DisplayName("초대 검증 - 실패 (shareId 불일치)")
    void verifyInvitationShareIdMismatch() {
        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("shareId", 1L)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        VerifyInvitationCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(VerifyInvitationCommand.class)
                .set("folderShare", folderShareEntity)
                .set("shareId", 999L) // 다른 shareId 사용
                .set("targetId", folderShareEntity.getTargetUser().getUserId())
                .set("folderId", folderShareEntity.getFolder().getFolderId())
                .sample();

        Assertions.assertThatThrownBy(() -> verifyInvitationUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_SHARE_ID);
    }

    @Test
    @DisplayName("초대 검증 - 실패 (초대 대상 X)")
    void verifyInvitationNotMine() {
        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("targetUser.userId", 1L)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        VerifyInvitationCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(VerifyInvitationCommand.class)
                .set("folderShare", folderShareEntity)
                .set("shareId", folderShareEntity.getShareId())
                .set("targetId", 999L) // 다른 targetId 사용
                .set("folderId", folderShareEntity.getFolder().getFolderId())
                .sample();

        Assertions.assertThatThrownBy(() -> verifyInvitationUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_DESERVE_ACCEPT_INVITATION);
    }

    @Test
    @DisplayName("초대 검증 - 실패 (folderId 불일치)")
    void verifyInvitationFolderIdMismatch() {
        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("folder.folderId", 1L)
                .set("invitationStatus", InvitationStatus.PENDING)
                .sample();

        VerifyInvitationCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(VerifyInvitationCommand.class)
                .set("folderShare", folderShareEntity)
                .set("shareId", folderShareEntity.getShareId())
                .set("targetId", folderShareEntity.getTargetUser().getUserId())
                .set("folderId", 999L) // 다른 folderId 사용
                .sample();

        Assertions.assertThatThrownBy(() -> verifyInvitationUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.MISMATCH_FOLDER_ID);
    }

    @Test
    @DisplayName("초대 검증 - 실패 (이미 수락된 초대)")
    void verifyInvitationAlreadyAccepted() {
        FolderShareEntity folderShareEntity = FixtureMonkey
                .builder()
                .objectIntrospector(BeanArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build()
                .giveMeBuilder(FolderShareEntity.class)
                .set("invitationStatus", InvitationStatus.ACCEPT)
                .sample();

        VerifyInvitationCommand command = FixtureMonkey
                .builder()
                .objectIntrospector(ConstructorPropertiesArbitraryIntrospector.INSTANCE)
                .build()
                .giveMeBuilder(VerifyInvitationCommand.class)
                .set("folderShare", folderShareEntity)
                .set("shareId", folderShareEntity.getShareId())
                .set("targetId", folderShareEntity.getTargetUser().getUserId())
                .set("folderId", folderShareEntity.getFolder().getFolderId())
                .sample();

        Assertions.assertThatThrownBy(() -> verifyInvitationUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVITE_ACCEPT_BAD_REQUEST);
    }
}

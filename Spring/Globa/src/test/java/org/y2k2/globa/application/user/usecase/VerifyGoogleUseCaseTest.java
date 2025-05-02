package org.y2k2.globa.application.user.usecase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
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
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.application.user.usecase.VerifyGoogleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

@Slf4j
@ExtendWith(MockitoExtension.class)
public class VerifyGoogleUseCaseTest {
    private VerifySnsCommand command;

    @InjectMocks
    private VerifyGoogleUseCase verifyGoogleUseCase;

    @Mock
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        command = new VerifySnsCommand("token", "snsId");
    }

    @Test
    @DisplayName("Google Token 검증 - 성공")
    void validateGoogleTest() throws FirebaseAuthException {
        FirebaseToken token = Mockito.mock(FirebaseToken.class);

        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenReturn(token);

        Mockito.when(token.getUid())
                .thenReturn(command.snsId());

        verifyGoogleUseCase.execute(command);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
        Mockito.verify(token, Mockito.times(1)).getUid();
    }

    @Test
    @DisplayName("Google Token 검증 - 실패 (토큰이 다름)")
    void validateGoogleFailTest() throws FirebaseAuthException {
        FirebaseToken token = Mockito.mock(FirebaseToken.class);

        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenReturn(token);

        Mockito.when(token.getUid())
                .thenReturn("invalid");

        Assertions.assertThatThrownBy(() -> verifyGoogleUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
        Mockito.verify(token, Mockito.times(1)).getUid();
    }

    @Test
    @DisplayName("Google Token 검증 - 실패 (FirebaseAuthException)")
    void validateGoogleFailFirebaseAuthExceptionTest() throws FirebaseAuthException {
        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenThrow(FirebaseAuthException.class);

        Assertions.assertThatThrownBy(() -> verifyGoogleUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
    }
}

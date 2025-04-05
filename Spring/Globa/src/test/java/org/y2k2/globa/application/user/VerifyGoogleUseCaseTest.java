package org.y2k2.globa.application.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.application.user.usecase.VerifyGoogleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

@Slf4j
@ExtendWith(SpringExtension.class)
public class VerifyGoogleUseCaseTest {
    private VerifySnsCommand command;

    private VerifyGoogleUseCase verifyGoogleUseCase;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        verifyGoogleUseCase = new VerifyGoogleUseCase(firebaseAuth);
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

package org.y2k2.globa.application.user;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.ConstructorPropertiesArbitraryIntrospector;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.y2k2.globa.application.user.command.ValidateSnsCommand;
import org.y2k2.globa.application.user.usecase.ValidateGoogleUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

@Slf4j
@ExtendWith(SpringExtension.class)
public class ValidateGoogleUseCaseTest {
    private ValidateSnsCommand command;

    private ValidateGoogleUseCase validateGoogleUseCase;

    @MockBean
    private FirebaseAuth firebaseAuth;

    @BeforeEach
    void setUp() {
        validateGoogleUseCase = new ValidateGoogleUseCase(firebaseAuth);
        command = new ValidateSnsCommand("token", "snsId");
    }

    @Test
    @DisplayName("Google Token 검증 성공")
    void validateGoogleTest() throws FirebaseAuthException {
        FirebaseToken token = Mockito.mock(FirebaseToken.class);

        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenReturn(token);

        Mockito.when(token.getUid())
                .thenReturn(command.snsId());

        validateGoogleUseCase.execute(command);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
        Mockito.verify(token, Mockito.times(1)).getUid();
    }

    @Test
    @DisplayName("Google Token 검증 실패 - 토큰이 다름")
    void validateGoogleFailTest() throws FirebaseAuthException {
        FirebaseToken token = Mockito.mock(FirebaseToken.class);

        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenReturn(token);

        Mockito.when(token.getUid())
                .thenReturn("invalid");

        Assertions.assertThatThrownBy(() -> validateGoogleUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
        Mockito.verify(token, Mockito.times(1)).getUid();
    }

    @Test
    @DisplayName("Google Token 검증 실패 - FirebaseAuthException 발생")
    void validateGoogleFailFirebaseAuthExceptionTest() throws FirebaseAuthException {
        Mockito.when(firebaseAuth.verifyIdToken(command.token()))
                .thenThrow(FirebaseAuthException.class);

        Assertions.assertThatThrownBy(() -> validateGoogleUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(firebaseAuth, Mockito.times(1)).verifyIdToken(command.token());
    }
}

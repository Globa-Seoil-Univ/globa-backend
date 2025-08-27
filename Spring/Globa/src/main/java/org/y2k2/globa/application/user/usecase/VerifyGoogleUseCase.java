package org.y2k2.globa.application.user.usecase;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;

@Slf4j
@RequiredArgsConstructor
@Component
public class VerifyGoogleUseCase implements VoidUseCase<VerifySnsCommand> {
    private final FirebaseAuth firebaseAuth;

    @Override
    public void execute(VerifySnsCommand command) {
        try {
            String tokenValue = command.token().replace("Bearer ", "");

            FirebaseToken token = firebaseAuth.verifyIdToken(tokenValue);

            if(!command.snsId().equalsIgnoreCase(token.getUid())){
                throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
            }
        } catch (FirebaseAuthException e) {
            log.error("Failed to verify firebase token : " + e);
            throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
        }
    }
}

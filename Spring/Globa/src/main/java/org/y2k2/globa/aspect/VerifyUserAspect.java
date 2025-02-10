package org.y2k2.globa.aspect;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.y2k2.globa.annotation.VerifyUser;
import org.y2k2.globa.entity.UserEntity;
import org.y2k2.globa.exception.CustomException;
import org.y2k2.globa.exception.ErrorCode;
import org.y2k2.globa.repository.UserRepository;
import org.y2k2.globa.util.jwt.JWTProvider;

@Slf4j
@RequiredArgsConstructor
@Aspect
@Component
public class VerifyUserAspect {
    private final HttpServletRequest request;
    private final JWTProvider provider;
    private final UserRepository userRepository;

    @Around("@annotation(org.y2k2.globa.annotation.VerifyUser)")
    public Object verifyUser(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        VerifyUser verifyUser = signature.getMethod().getAnnotation(VerifyUser.class);
        boolean isVerify = verifyUser.isVerify();

        String accessToken = request.getHeader("Authorization");
        Long userId = provider.getUserIdByAccessToken(accessToken);

        UserEntity user;

        if (isVerify) {
            user = userRepository.findById(userId)
                    .orElseThrow(() -> new CustomException(ErrorCode.NOT_FOUND_USER));

            if (user.getIsDeleted()) {
                throw new CustomException(ErrorCode.DELETED_USER);
            }
        } else {
            user = userRepository.findById(userId).orElse(null);
        }

        log.info("Verify user = {}, isVerify = {}", user, isVerify);

        // 마지막 인자 값에 추가
        Object[] args = joinPoint.getArgs();
        args[args.length - 1] = user;

        return joinPoint.proceed(args);
    }
}

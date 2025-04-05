package org.y2k2.globa.application.user;

import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.*;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.application.user.usecase.VerifyKakaoUseCase;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

@ExtendWith(SpringExtension.class)
@Slf4j
public class VerifyKakaoUseCaseTest {
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private VerifySnsCommand command;
    private VerifyKakaoUseCase validateKakaoSnsUseCase;

    @MockBean
    private RestTemplate restTemplate;

    @BeforeEach
    void setUp() {
        validateKakaoSnsUseCase = new VerifyKakaoUseCase(restTemplate);
        command = new VerifySnsCommand("12345", "valid_token");
    }

    @Test
    @DisplayName("카카오 토큰 검증 - 성공")
    void validateKakaoTokenSuccess() {
        ResponseEntity<String> response = ResponseEntity.ok(generateResponse(command.snsId()));

        Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        )).thenReturn(response);

        Assertions.assertThatNoException().isThrownBy(() -> validateKakaoSnsUseCase.execute(command));

        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        );
    }

    @Test
    @DisplayName("카카오 토큰 검증 - 실패 (불일치)")
    void validateKakaoTokenFailMismatchTest() {
        ResponseEntity<String> response = ResponseEntity.ok(generateResponse("54321"));

        Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        )).thenReturn(response);

        Assertions.assertThatThrownBy(() -> validateKakaoSnsUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        );
    }

    @Test
    @DisplayName("카카오 토큰 검증 - 실패 (잘못된 토큰)")
    void validateKakaoTokenFailTest() {
        Mockito.when(restTemplate.exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        )).thenThrow(RestClientException.class);

        Assertions.assertThatThrownBy(() -> validateKakaoSnsUseCase.execute(command))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_TOKEN);

        Mockito.verify(restTemplate, Mockito.times(1)).exchange(
                ArgumentMatchers.eq(KAKAO_USER_INFO_URL),
                ArgumentMatchers.eq(HttpMethod.GET),
                ArgumentMatchers.any(HttpEntity.class),
                ArgumentMatchers.eq(String.class)
        );
    }

    private String generateResponse(String snsId) {
        return """
                {
                    "id": %s,
                    "connected_at": "2024-03-29T10:00:00Z",
                    "properties": {
                        "nickname": "test_user"
                    }
                }
                """.formatted(snsId);
    }
}


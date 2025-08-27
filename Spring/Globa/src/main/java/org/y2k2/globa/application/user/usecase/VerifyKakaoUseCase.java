package org.y2k2.globa.application.user.usecase;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.y2k2.globa.application.user.command.VerifySnsCommand;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;
import org.y2k2.globa.common.usecase.VoidUseCase;

@Slf4j
@RequiredArgsConstructor
@Component
public class VerifyKakaoUseCase implements VoidUseCase<VerifySnsCommand> {
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    private final RestTemplate restTemplate;

    public void execute(VerifySnsCommand command) {
        try {
            // HTTP 요청 헤더에 Authorization 추가
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", command.token());

            HttpEntity<String> entity = new HttpEntity<>(headers);
            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            // JSON 응답을 JsonNode로 파싱
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode responseBody = objectMapper.readTree(response.getBody());
            String kakaoUID = String.valueOf(responseBody.get("id"));

            if(!command.snsId().equalsIgnoreCase(kakaoUID)) {
                throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
            }
        } catch (Exception e) {
            log.error("Failed to verify kakao token : " + e);
            throw new CustomException(ErrorCode.INVALID_SNS_TOKEN);
        }
    }
}

package org.y2k2.globa.exception;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.springframework.http.ResponseEntity;
import org.y2k2.globa.util.CustomTimestamp;

@Getter
@Builder
public class ErrorResponse {
    private final String timestamp = new CustomTimestamp().toString();
    private final int status;
    private final String error;
    private final String message;
    private final String code;
    private final int errorCode;

    public static ResponseEntity<ErrorResponse> toResponseEntity(ErrorCode errorCode) {
        return ResponseEntity
                .status(errorCode.getHttpStatus())
                .body(ErrorResponse.builder()
                        .status(errorCode.getHttpStatus().value())
                        .errorCode(errorCode.getErrorCode())
                        .error(errorCode.getHttpStatus().name())
                        .code(errorCode.name())
                        .message(errorCode.getMessage())
                        .build()
                );
    }

    public static String toJson(ErrorCode errorCode) throws JsonProcessingException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.writeValueAsString(
                ErrorResponse.toResponseEntity(errorCode).getBody()
        );
    }
}

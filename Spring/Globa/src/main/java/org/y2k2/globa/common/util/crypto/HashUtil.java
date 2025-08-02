package org.y2k2.globa.common.util.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.codec.Hex;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

@Component
public class HashUtil {
    @Value("${spring.security.salt}")
    private String salt;

    public String hash(String value) {
        if (salt == null || salt.isEmpty()) {
            throw new CustomException(ErrorCode.NOT_FOUND_HASH_SALT);
        } else if (value == null || value.isEmpty() || value.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_SNS_ID);
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            String saltedValue = salt + value + salt;
            byte[] hashBytes = digest.digest(saltedValue.getBytes(StandardCharsets.UTF_8));

            return new String(Hex.encode(hashBytes));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_HASH);
        }
    }
}

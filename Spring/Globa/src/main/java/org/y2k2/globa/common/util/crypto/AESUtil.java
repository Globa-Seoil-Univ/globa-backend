package org.y2k2.globa.common.util.crypto;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Component
public class AESUtil {
    @Value("${spring.security.secret_key}")
    private String secretKey;

    private static final String ALGORITHM = "AES";
    private static final Integer LENGTH = 128;
    private static final String TRANSFORMATION = "AES/CBC/PKCS5Padding";

    public String encrypt(Long value) {
        if (value == null) {
            throw new CustomException(ErrorCode.INVALID_AES_ENCRYPTION_VALUE);
        }

        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            keyBytes = Arrays.copyOf(keyBytes, LENGTH / 8);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = new byte[cipher.getBlockSize()];
            new SecureRandom().nextBytes(iv);
            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] encrypted = cipher.doFinal(value.toString().getBytes(StandardCharsets.UTF_8));
            byte[] encryptedWithIv = concatenate(iv, encrypted);
            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_AES_ENCRYPTION);
        }
    }

    public Long decrypt(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty() || encryptedValue.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_AES_DECRYPTION_VALUE);
        }

        try {
            byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
            keyBytes = Arrays.copyOf(keyBytes, LENGTH / 8);

            SecretKeySpec secretKeySpec = new SecretKeySpec(keyBytes, ALGORITHM);

            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedValue);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            byte[] iv = Arrays.copyOfRange(encryptedWithIv, 0, cipher.getBlockSize());
            byte[] encrypted = Arrays.copyOfRange(encryptedWithIv, cipher.getBlockSize(), encryptedWithIv.length);

            IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

            byte[] decrypted = cipher.doFinal(encrypted);

            String userIdStr = new String(decrypted, StandardCharsets.UTF_8);
            return Long.parseLong(userIdStr);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FAILED_AES_DECRYPTION);
        }
    }

    private byte[] concatenate(byte[] iv, byte[] encrypted) {
        byte[] result = new byte[iv.length + encrypted.length];
        System.arraycopy(iv, 0, result, 0, iv.length);
        System.arraycopy(encrypted, 0, result, iv.length, encrypted.length);
        return result;
    }
}

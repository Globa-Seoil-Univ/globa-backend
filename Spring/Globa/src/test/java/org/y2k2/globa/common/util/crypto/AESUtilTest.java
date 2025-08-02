package org.y2k2.globa.common.util.crypto;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.y2k2.globa.common.exception.CustomException;
import org.y2k2.globa.common.exception.ErrorCode;

import static org.assertj.core.api.Assertions.*;

@Slf4j
@ExtendWith(MockitoExtension.class)
class AESUtilTest {
    private AESUtil aesUtil;
    private static final String TEST_SECRET_KEY = "testSecretKey123456789012345"; // 32바이트 키

    @BeforeEach
    void setUp() {
        aesUtil = new AESUtil();
        ReflectionTestUtils.setField(aesUtil, "secretKey", TEST_SECRET_KEY);
    }

    @Test
    @DisplayName("암호화/복호화 - 성공")
    void encryptAndDecrypt_Success() {
        Long originalValue = 12345L;

        String encryptedValue = aesUtil.encrypt(originalValue);
        log.info("Encrypted Value = {}", encryptedValue);
        Long decryptedValue = aesUtil.decrypt(encryptedValue);
        log.info("Decrypted Value = {}", decryptedValue);

        assertThat(decryptedValue).isEqualTo(originalValue);
        assertThat(encryptedValue).isNotNull();
        assertThat(encryptedValue).isNotEmpty();
    }

    @Test
    @DisplayName("암호화/복호화 - 성공 (다중값)")
    void encryptAndDecrypt_MultipleValues() {
        Long[] testValues = {1L, 100L, 999999L, Long.MAX_VALUE, 0L};

        for (Long testValue : testValues) {
            String encrypted = aesUtil.encrypt(testValue);
            Long decrypted = aesUtil.decrypt(encrypted);

            assertThat(decrypted).isEqualTo(testValue);
        }
    }

    @Test
    @DisplayName("암호화 - 성공 (IV에 의한 같은 인자, 다른 결과)")
    void encrypt_SameValue_DifferentResults() {
        Long testValue = 12345L;

        String encrypted1 = aesUtil.encrypt(testValue);
        String encrypted2 = aesUtil.encrypt(testValue);

        assertThat(encrypted1).isNotEqualTo(encrypted2);

        assertThat(aesUtil.decrypt(encrypted1)).isEqualTo(testValue);
        assertThat(aesUtil.decrypt(encrypted2)).isEqualTo(testValue);
    }

    @Test
    @DisplayName("암호화 - 성공 (암호화된 값이 Base64 형식)")
    void encrypt_ReturnsValidBase64() {
        Long testValue = 12345L;

        String encrypted = aesUtil.encrypt(testValue);

        assertThatCode(() -> {
            byte[] decoded = java.util.Base64.getDecoder().decode(encrypted);
            assertThat(decoded).isNotEmpty();
        })
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("암호화/복호화 - 성공 (큰 숫자 값)")
    void encryptAndDecrypt_LargeNumbers() {
        Long largeValue = 9223372036854775807L; // Long.MAX_VALUE

        String encrypted = aesUtil.encrypt(largeValue);
        Long decrypted = aesUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(largeValue);
    }

    @Test
    @DisplayName("암호화 - 성공 (짧은 secretKey)")
    void encryptAndDecrypt_ShortSecretKey() {
        ReflectionTestUtils.setField(aesUtil, "secretKey", "short"); // 짧은 키
        Long testValue = 12345L;

        String encrypted = aesUtil.encrypt(testValue);
        Long decrypted = aesUtil.decrypt(encrypted);

        assertThat(decrypted).isEqualTo(testValue);
    }

    @Test
    @DisplayName("암호화 - 실패 (secretKey null)")
    void encrypt_NullSecretKey_ThrowsException() {
        ReflectionTestUtils.setField(aesUtil, "secretKey", null);
        Long testValue = 12345L;

        assertThatThrownBy(() -> aesUtil.encrypt(testValue))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_AES_ENCRYPTION);
    }

    @Test
    @DisplayName("암호화 - 실패 (null 값)")
    void encrypt_NullValue_ThrowsException() {
        assertThatThrownBy(() -> aesUtil.encrypt(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AES_ENCRYPTION_VALUE);
    }

    @Test
    @DisplayName("복호화 - 실패 (잘못된 암호화 문자열)")
    void decrypt_InvalidEncryptedValue_ThrowsException() {
        String invalidEncryptedValue = "invalid_encrypted_string";

        assertThatThrownBy(() -> aesUtil.decrypt(invalidEncryptedValue))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_AES_DECRYPTION);
    }

    @Test
    @DisplayName("복호화 - 실패 (null 암호화 문자열)")
    void decrypt_NullValue_ThrowsException() {
        assertThatThrownBy(() -> aesUtil.decrypt(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AES_DECRYPTION_VALUE);
    }

    @Test
    @DisplayName("복호화 - 실패 (빈 문자열)")
    void decrypt_EmptyString_ThrowsException() {
        assertThatThrownBy(() -> aesUtil.decrypt(""))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_AES_DECRYPTION_VALUE);
    }

    @Test
    @DisplayName("복호화 - 실패 (Base64가 아닌 문자열)")
    void decrypt_NonBase64String_ThrowsException() {
        String nonBase64String = "this_is_not_base64!@#$%";

        assertThatThrownBy(() -> aesUtil.decrypt(nonBase64String))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_AES_DECRYPTION);
    }

    @Test
    @DisplayName("복호화 - 실패 (잘못된 길이의 암호화 문자열)")
    void decrypt_InvalidLength_ThrowsException() {
        // 너무 짧은 Base64 문자열 (IV 길이보다 짧음)
        String shortBase64 = "dGVzdA==";

        assertThatThrownBy(() -> aesUtil.decrypt(shortBase64))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.FAILED_AES_DECRYPTION);
    }
}

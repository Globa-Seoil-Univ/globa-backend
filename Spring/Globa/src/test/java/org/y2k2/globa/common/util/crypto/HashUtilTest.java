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
class HashUtilTest {
    private HashUtil hashUtil;
    private static final String TEST_SALT = "testSalt123";

    @BeforeEach
    void setUp() {
        hashUtil = new HashUtil();
        ReflectionTestUtils.setField(hashUtil, "salt", TEST_SALT);
    }

    @Test
    @DisplayName("해시 - 성공")
    void hash_Success() {
        String originalValue = "testPassword123";

        String hashedValue = hashUtil.hash(originalValue);
        log.info("Original Value = {}", originalValue);
        log.info("Hashed Value = {}", hashedValue);

        assertThat(hashedValue).isNotNull();
        assertThat(hashedValue).isNotEmpty();
        assertThat(hashedValue).isNotEqualTo(originalValue);
        assertThat(hashedValue).hasSize(64); // SHA-256은 64자리 hex 문자열
    }

    @Test
    @DisplayName("해시 - 성공 (다중값)")
    void hash_MultipleValues() {
        String[] testValues = {"password1", "password2", "test123", "admin", "user@example.com"};

        for (String testValue : testValues) {
            String hashed = hashUtil.hash(testValue);
            log.info("Original: {} -> Hashed: {}", testValue, hashed);

            assertThat(hashed).isNotNull();
            assertThat(hashed).isNotEmpty();
            assertThat(hashed).hasSize(64);
            assertThat(hashed).isNotEqualTo(testValue);
        }
    }

    @Test
    @DisplayName("해시 - 성공 (같은 값은 항상 같은 해시)")
    void hash_SameValue_SameHash() {
        String testValue = "consistentPassword";

        String hash1 = hashUtil.hash(testValue);
        String hash2 = hashUtil.hash(testValue);
        log.info("Hash1 = {}", hash1);
        log.info("Hash2 = {}", hash2);

        assertThat(hash1).isEqualTo(hash2);
    }

    @Test
    @DisplayName("해시 - 성공 (다른 값은 다른 해시)")
    void hash_DifferentValues_DifferentHashes() {
        String value1 = "password1";
        String value2 = "password2";

        String hash1 = hashUtil.hash(value1);
        String hash2 = hashUtil.hash(value2);
        log.info("Value1: {} -> Hash1: {}", value1, hash1);
        log.info("Value2: {} -> Hash2: {}", value2, hash2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("해시 - 성공 (특수문자 포함)")
    void hash_SpecialCharacters_Success() {
        String specialValue = "password!@#$%^&*()_+{}|:<>?[]\\;',./";

        String hashedValue = hashUtil.hash(specialValue);
        log.info("Special Characters Hashed Value = {}", hashedValue);

        assertThat(hashedValue).isNotNull();
        assertThat(hashedValue).isNotEmpty();
        assertThat(hashedValue).hasSize(64);
    }

    @Test
    @DisplayName("해시 - 성공 (긴 문자열)")
    void hash_LongString_Success() {
        String longValue = "a".repeat(1000);

        String hashedValue = hashUtil.hash(longValue);
        log.info("Long String Hashed Value = {}", hashedValue);

        assertThat(hashedValue).isNotNull();
        assertThat(hashedValue).isNotEmpty();
        assertThat(hashedValue).hasSize(64);
    }

    @Test
    @DisplayName("해시 - 성공 (유니코드 문자열)")
    void hash_UnicodeString_Success() {
        String unicodeValue = "한글패스워드123";

        String hashedValue = hashUtil.hash(unicodeValue);
        log.info("Unicode String Hashed Value = {}", hashedValue);

        assertThat(hashedValue).isNotNull();
        assertThat(hashedValue).isNotEmpty();
        assertThat(hashedValue).hasSize(64);
    }

    @Test
    @DisplayName("해시 - 성공 (다른 salt는 다른 해시)")
    void hash_DifferentSalt_DifferentHash() {
        String testValue = "password";

        String hash1 = hashUtil.hash(testValue);

        ReflectionTestUtils.setField(hashUtil, "salt", "differentSalt");
        String hash2 = hashUtil.hash(testValue);

        log.info("Same value with different salt:");
        log.info("Hash1 = {}", hash1);
        log.info("Hash2 = {}", hash2);

        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("해시 - 성공 (해시 결과가 hex 형식)")
    void hash_ReturnsValidHex() {
        String testValue = "password";

        String hashedValue = hashUtil.hash(testValue);

        assertThat(hashedValue).matches("^[0-9a-f]{64}$");
    }

    @Test
    @DisplayName("해시 - 성공 (salt 적용 확인)")
    void hash_SaltApplied_Success() {
        String testValue = "password";
        String testSalt1 = "salt1";
        String testSalt2 = "salt2";

        ReflectionTestUtils.setField(hashUtil, "salt", testSalt1);
        String hash1 = hashUtil.hash(testValue);

        ReflectionTestUtils.setField(hashUtil, "salt", testSalt2);
        String hash2 = hashUtil.hash(testValue);

        log.info("Same value with salt1 = {}", hash1);
        log.info("Same value with salt2 = {}", hash2);

        assertThat(hash1).isNotEqualTo(hash2);
        assertThat(hash1).hasSize(64);
        assertThat(hash2).hasSize(64);
    }

    @Test
    @DisplayName("해시 - 실패 (빈 문자열)")
    void hash_EmptyString_ThrowsException() {
        String emptyValue = "";

        assertThatThrownBy(() -> hashUtil.hash(emptyValue))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_ID);
    }

    @Test
    @DisplayName("해시 - 실패 (공백 문자열)")
    void hash_WhitespaceString_ThrowsException() {
        String whitespaceValue = "   ";

        assertThatThrownBy(() -> hashUtil.hash(whitespaceValue))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_ID);
    }

    @Test
    @DisplayName("해시 - 실패 (null 값)")
    void hash_NullValue_ThrowsException() {
        assertThatThrownBy(() -> hashUtil.hash(null))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_SNS_ID);
    }

    @Test
    @DisplayName("해시 - 실패 (salt null)")
    void hash_NullSalt_ThrowsException() {
        ReflectionTestUtils.setField(hashUtil, "salt", null);
        String testValue = "password";

        assertThatThrownBy(() -> hashUtil.hash(testValue))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.NOT_FOUND_HASH_SALT);
    }
}

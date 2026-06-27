package com.railmind.common;

import com.railmind.common.util.CryptoUtil;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptoUtilTest {

    @Test
    void aesEncryptDecrypt_shouldRoundTrip() {
        String original = "110101199001011234";
        String encrypted = CryptoUtil.aesEncrypt(original);
        String decrypted = CryptoUtil.aesDecrypt(encrypted);
        assertEquals(original, decrypted);
    }

    @Test
    void aesEncrypt_shouldNotEqualOriginal() {
        String original = "110101199001011234";
        String encrypted = CryptoUtil.aesEncrypt(original);
        assertNotEquals(original, encrypted);
    }

    @Test
    void sha256_shouldReturnFixedLength() {
        String hash = CryptoUtil.sha256("110101199001011234");
        assertEquals(64, hash.length());
    }

    @Test
    void sha256_sameInputSameOutput() {
        String hash1 = CryptoUtil.sha256("110101199001011234");
        String hash2 = CryptoUtil.sha256("110101199001011234");
        assertEquals(hash1, hash2);
    }

    @Test
    void sha256_differentInputDifferentOutput() {
        String hash1 = CryptoUtil.sha256("110101199001011234");
        String hash2 = CryptoUtil.sha256("110101199001011235");
        assertNotEquals(hash1, hash2);
    }

    @Test
    void maskIdCard_shouldMask() {
        String masked = CryptoUtil.maskIdCard("110101199001011234");
        assertEquals("110***********1234", masked);
    }

    @Test
    void maskPhone_shouldMask() {
        String masked = CryptoUtil.maskPhone("13812345678");
        assertEquals("138****5678", masked);
    }

    @Test
    void aesEncrypt_nullInput_shouldReturnNull() {
        assertNull(CryptoUtil.aesEncrypt(null));
    }

    @Test
    void aesDecrypt_nullInput_shouldReturnNull() {
        assertNull(CryptoUtil.aesDecrypt(null));
    }
}

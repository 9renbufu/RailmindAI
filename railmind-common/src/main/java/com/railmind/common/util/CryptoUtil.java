package com.railmind.common.util;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

/**
 * 加密工具类: AES-256加密身份证 + SHA256哈希
 */
public class CryptoUtil {

    private static final String AES_ALGORITHM = "AES";
    private static final String AES_TRANSFORMATION = "AES/ECB/PKCS5Padding";
    // 生产环境应从配置中心读取，这里用默认值
    private static String secretKey = "RailMind2026SecretKey1234567890a";

    public static void setSecretKey(String key) {
        secretKey = key;
    }

    /**
     * AES加密
     */
    public static String aesEncrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            throw new RuntimeException("AES加密失败", e);
        }
    }

    /**
     * AES解密
     */
    public static String aesDecrypt(String cipherText) {
        if (cipherText == null || cipherText.isEmpty()) {
            return cipherText;
        }
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8), AES_ALGORITHM);
            Cipher cipher = Cipher.getInstance(AES_TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.getDecoder().decode(cipherText);
            return new String(cipher.doFinal(decoded), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new RuntimeException("AES解密失败", e);
        }
    }

    /**
     * SHA256哈希(用于身份证唯一性校验)
     */
    public static String sha256(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("SHA256哈希失败", e);
        }
    }

    /**
     * 身份证号脱敏: 110***********1234
     */
    public static String maskIdCard(String idCard) {
        if (idCard == null || idCard.length() < 8) {
            return idCard;
        }
        return idCard.substring(0, 3) + "***********" + idCard.substring(idCard.length() - 4);
    }

    /**
     * 手机号脱敏: 138****5678
     */
    public static String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }
}

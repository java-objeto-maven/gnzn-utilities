package org.guanzon.gnzn.utilities.mis;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

public class AESEncrypt {
    private static byte[] sha256Bytes(String key) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        return md.digest(key.getBytes(StandardCharsets.UTF_8)); // 32 bytes
    }

    private static String encryptBase64(String plaintext, String key) throws Exception {
        byte[] aesKey = sha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.ENCRYPT_MODE, keySpec);

        byte[] encrypted = cipher.doFinal(plaintext.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(encrypted);
    }

    private static String decryptBase64(String base64Cipher, String key) throws Exception {
        byte[] aesKey = sha256Bytes(key);
        SecretKeySpec keySpec = new SecretKeySpec(aesKey, "AES");

        Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, keySpec);

        byte[] decoded = Base64.getDecoder().decode(base64Cipher);
        byte[] decrypted = cipher.doFinal(decoded);
        return new String(decrypted, StandardCharsets.UTF_8);
    }

    // quick test
    public static void main(String[] args) throws Exception {
        String key = "empid";
        String plain = "C00109001231";
//        String enc = encryptBase64(plain, key);
//        System.out.println("Encrypted (base64): " + enc);
//        System.out.println("Decrypted: " + decryptBase64(enc, key));
        
        plain = "IdEBrD+xc+OpkEGkH8DSaQ==";
        System.out.println("Decrypted: " + decryptBase64(plain, key));
    }
}
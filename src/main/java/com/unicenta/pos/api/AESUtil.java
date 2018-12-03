package com.unicenta.pos.api;


import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class AESUtil {
    private static SecretKey key;
    static Cipher cipher;

    private static final String ALGORITHM = "AES/ECB/PKCS5Padding";

    public AESUtil(SecretKey key) throws NoSuchPaddingException, NoSuchAlgorithmException {
        this.key = key;
        cipher = Cipher.getInstance("AES");
    }

    public static SecretKey generateKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("AES");
        keyGen.init(256); // for example
        SecretKey secretKey = keyGen.generateKey();
        return secretKey;
    }

    public static SecretKey decodeB64Key(String encodedKey) {
        byte[] decodedKey = Base64.getDecoder().decode(encodedKey);
        // rebuild key using SecretKeySpec
        SecretKey originalKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
        return originalKey;
    }

    public static String encrypt(String plainText)
            throws Exception {
        byte[] plainTextByte = plainText.getBytes();
        cipher.init(Cipher.ENCRYPT_MODE, key);
        byte[] encryptedByte = cipher.doFinal(plainTextByte);
        Base64.Encoder encoder = Base64.getEncoder();
        String encryptedText = encoder.encodeToString(encryptedByte);
        return encryptedText;
    }

    public static String decrypt(String encryptedText)
            throws Exception {
        Base64.Decoder decoder = Base64.getDecoder();
        byte[] encryptedTextByte = decoder.decode(encryptedText);
        cipher.init(Cipher.DECRYPT_MODE, key);
        byte[] decryptedByte = cipher.doFinal(encryptedTextByte);
        String decryptedText = new String(decryptedByte);
        return decryptedText;
    }


//    public String encryptString(String str) throws Exception {
//        byte[] bytes = encrypt(str.getBytes(StandardCharsets.UTF_8));
//        return new String(bytes);
//    }
//
//    public String decryptString(String str) throws Exception {
//        byte[] bytes = decrypt(str.getBytes(StandardCharsets.UTF_8));
//        return new String(bytes);
//    }
}
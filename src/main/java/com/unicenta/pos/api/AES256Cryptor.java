package com.unicenta.pos.api;

/**
 * Created by kientux on 3/20/15
 * https://gist.githubusercontent.com/kientux/bb48259c6f2133e628ad/raw/3d38015e43ed0cc88e59c0fd1af43231ffbf6403/AES256Cryptor.java
 * <p>
 * NOTE this enables Crypto-js (tested with 3.1.9-1) and Java to encrypt/decrypt AES
 * It is an OpenSSL compatible implementation
 * <p>
 * JS example:
 * var encoded=CryptoJS.AES.encrypt(original,key).toString(CryptoJS.enc.Utf8)
 * var decoded=CryptoJS.AES.decrypt(encoded,key).toString(CryptoJS.enc.Utf8)
 */

import java.security.GeneralSecurityException;
import java.util.Base64;


import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;
import java.util.logging.Logger;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import com.unicenta.pos.api.AesCbcWithIntegrity.*;

public class AES256Cryptor {

    private static final Logger logger = Logger.getLogger("AES256Cryptor");

    public static String getKeysStr() {
        return keysStr;
    }

    public static void setKeysStr(String keysStr) {
        AES256Cryptor.keysStr = keysStr;
    }

    public static String generateKeys(String passphrase) {
        byte[] salt = new byte[0];
        AesCbcWithIntegrity.SecretKeys keys;
        try {
            salt = AesCbcWithIntegrity.generateSalt();
            keys = AesCbcWithIntegrity.generateKeyFromPassword(
                    passphrase, salt
            );
            keysStr = keys.toString();
            return keysStr;
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        }
        return null;
    }

    private static String keysStr; //generated iv+mac

    /**
     * Encrypt
     *
     * @param plaintext  plain string
     * @param passphrase passphrase
     * @return
     */
    public static String encrypt(String plaintext, String passphrase) {
        try {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(keysStr);
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac = AesCbcWithIntegrity.encrypt(plaintext, keys);
            //store or send to server
            String ciphertextString = cipherTextIvMac.toString();
            return ciphertextString;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Decrypt
     * Thanks Artjom B. for this: http://stackoverflow.com/a/29152379/4405051
     *
     * @param ciphertext encrypted string
     * @param passphrase passphrase
     */
    public static String decrypt(String ciphertext, String passphrase) {
        try {
            AesCbcWithIntegrity.SecretKeys keys = AesCbcWithIntegrity.keys(keysStr);
            AesCbcWithIntegrity.CipherTextIvMac cipherTextIvMac =
                    new AesCbcWithIntegrity.CipherTextIvMac(ciphertext);

            String plainText = AesCbcWithIntegrity.decryptString(cipherTextIvMac, keys);
            return plainText;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }


}

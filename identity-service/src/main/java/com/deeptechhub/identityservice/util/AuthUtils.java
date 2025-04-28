package com.deeptechhub.identityservice.util;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class generates a Base64 SHA256 encoded secret key
 */
public class AuthUtils {
    public static String generateBase64EncodedSecretKey() throws NoSuchAlgorithmException {
        KeyGenerator keyGen = KeyGenerator.getInstance("HmacSHA256");
        keyGen.init(256); // 256-bit key size
        byte[] secretKeyBytes = keyGen.generateKey().getEncoded();
        return Base64.getEncoder().encodeToString(secretKeyBytes);
    }

    public static void main(String[] args) throws NoSuchAlgorithmException {
        String secretKey = generateBase64EncodedSecretKey();
        System.out.println("Base64 Secret Key=" + secretKey);
    }
}


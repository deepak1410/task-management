package com.deeptechhub.identityservice.util;

import com.deeptechhub.identityservice.filter.JwtAuthenticationFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.crypto.KeyGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

/**
 * This class generates a Base64 SHA256 encoded secret key
 */
public class AuthUtils {
    private static final Logger log = LoggerFactory.getLogger(AuthUtils.class);

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

    public static String extractToken(HttpServletRequest request) {
        final String authHeader = request.getHeader("Authorization");
        log.debug("authHeader has been retrieved as {}", authHeader);
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("No auth token could be found in the request header");
            return null;
        }
        return authHeader.substring(7);
    }
}


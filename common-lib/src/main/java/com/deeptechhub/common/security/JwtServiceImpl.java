package com.deeptechhub.common.security;

import com.deeptechhub.common.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    private final String jwtSecret;
    private final long accessTokenExpiryMs;
    private final long refreshTokenExpiryMs;
    private SecretKey secretKey;
    private JwtParser jwtParser;

    public JwtServiceImpl(
            //@Qualifier("jwtSecret") String jwtSecret,
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.accessTokenExpiryMs}") long accessTokenExpiryMs,
            @Value("${jwt.refreshTokenExpiryMs}") long refreshTokenExpiryMs) {
        this.jwtSecret = jwtSecret;
        this.accessTokenExpiryMs = accessTokenExpiryMs;
        this.refreshTokenExpiryMs = refreshTokenExpiryMs;
        initialize();
    }

    @PostConstruct
    public void initialize() {
        try {
            this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
            this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
        } catch (Exception e) {
            throw new JwtAuthenticationException("Failed to initialize JWT service", e);
        }
    }

    @Override
    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, String username) {
        final String usernameFromToken = extractUsername(token);
        return usernameFromToken.equals(username) && !isTokenExpired(token);
    }

    @Override
    public String generateAccessToken(String username) {
        return generateToken(username, accessTokenExpiryMs);
    }

    @Override
    public String generateRefreshToken(String username) {
        return generateToken(username, refreshTokenExpiryMs);
    }

    private String generateToken(String username, long expiryMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiryMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private boolean isTokenExpired(String token) {
        return getClaim(token, Claims::getExpiration).before(new Date());
    }

    private <T> T getClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaims(String token) {
        try {
            return jwtParser.parseSignedClaims(token).getPayload();
        } catch (JwtException ex) {
            throw new JwtAuthenticationException("Invalid JWT token", ex);
        }
    }
}
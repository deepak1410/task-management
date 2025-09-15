package com.deeptechhub.taskservice.security;

import com.deeptechhub.common.dto.UserDto;
import com.deeptechhub.taskservice.exception.JwtAuthenticationException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

@Service
public class JwtServiceImpl implements JwtService {
    private static final Logger log = LoggerFactory.getLogger(JwtServiceImpl.class);
    private final String jwtSecret;
    private SecretKey secretKey;
    private JwtParser jwtParser;

    public JwtServiceImpl(
            @Qualifier("jwtSecret") String jwtSecret) {
        this.jwtSecret = jwtSecret;
        init();
    }

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
        this.jwtParser = Jwts.parser().verifyWith(secretKey).build();
    }

    @Override
    public String extractUsername(String token) {
        return getClaim(token, Claims::getSubject);
    }

    @Override
    public boolean isTokenValid(String token, UserDto userDto) {
        final String username = extractUsername(token);
        return username.equals(userDto.getUsername()) && !isTokenExpired(token);
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
            log.warn("Invalid JWT token: {}", ex.getMessage());
            throw new JwtAuthenticationException("Invalid JWT token");
        }
    }

}


### Security Hardening
* Add this in the identity-service later
``` 
// In identity-service
@Bean
public JwtEncoder jwtEncoder(@Value("${jwt.key.id}") String keyId) {
    JWKSource<SecurityContext> jwkSource = new ImmutableJWKSet<>(
        new JWKSet(new RSAKey.Builder(publicKey)
            .privateKey(privateKey)
            .keyID(keyId)
            .build()));
    return new NimbusJwtEncoder(jwkSource);
}
```
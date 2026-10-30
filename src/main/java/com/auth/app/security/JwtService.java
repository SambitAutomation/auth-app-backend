package com.auth.app.security;

import com.auth.app.model.Role;
import com.auth.app.model.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Data
public class JwtService {

    private final SecretKey key;
    private final long accessTtlSecond;
    private final long refreshTtlSecond;
    private final String issuer;

    public JwtService(@Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.access-ttl-seconds}") long accessTtlSecond,
                      @Value("${security.jwt.refresh-ttl-seconds}") long refreshTtlSecond,
                      @Value("${security.jwt.issuer}") String issuer){

        if(secret == null || secret.length() < 10){
            throw new IllegalArgumentException("Invalid Secret !!!");
        }

        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTtlSecond = accessTtlSecond;
        this.refreshTtlSecond = refreshTtlSecond;
        this.issuer = issuer;
    }

    // generate Token
    public  String generateToken(User user){
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return Jwts.builder()
                .id(UUID.randomUUID().toString())
                .subject(user.getId().toString())
                .issuer(issuer)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(accessTtlSecond)))
                .claims(
                        Map.of(
                                "email", user.getEmail(),
                                "role", roles,
                                "typ", "access"
                        )
                )
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // refresh token
    public String generateRefreshToken(User user, String jti){
        Instant now = Instant.now();
        List<String> roles = user.getRoles() == null ? List.of() :
                user.getRoles().stream().map(Role::getName).collect(Collectors.toList());

        return Jwts.builder()
                .id(jti)
                .subject(user.getId().toString())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(refreshTtlSecond)))
                .claims(Map.of("typ", "refresh"))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    // parse token
    public Jws<Claims> parseToken(String token){
        try{
            return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
        }catch (JwtException e){
            throw e;
        }
    }

    public  boolean isAccessToken(String token){
        Claims c = parseToken(token).getPayload();
        return "access".equals(c.get("typ"));
    }

    public  boolean isRefreshToken(String token){
        Claims c = parseToken(token).getPayload();
        return "refresh".equals(c.get("typ"));
    }

    public UUID getUserId(String token){
        Claims c = parseToken(token).getPayload();
        return  UUID.fromString(c.getSubject());
    }

    public String getJti(String token){
        return  parseToken(token).getPayload().getId();
    }

    public List<String> getRoles(String token){
        Claims c = parseToken(token).getPayload();
        return (List<String>) c.get("roles");
    }

    public String getEmail(String token){
        Claims c = parseToken(token).getPayload();
        return (String) c.get("email");
    }
}

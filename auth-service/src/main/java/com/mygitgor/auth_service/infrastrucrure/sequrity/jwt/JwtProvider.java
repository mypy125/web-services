package com.mygitgor.auth_service.infrastrucrure.sequrity.jwt;

import com.mygitgor.auth_service.domain.auth.model.enums.UserRole;
import com.mygitgor.auth_service.infrastrucrure.client.UserServiceClient;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Getter
@Service
@RequiredArgsConstructor
public class JwtProvider {
    private final JwtProps jwtProps;
    private final UserServiceClient userClient;
    private SecretKey key;

    @PostConstruct
    private void init() {
        this.key = Keys.hmacShaKeyFor(jwtProps.getSecretKey().getBytes(StandardCharsets.UTF_8));
        log.info("JwtProvider initialized");
    }

    public String generateToken(String email, UserRole role, String userId) {
        return generateToken(email, List.of(role.name()), userId);
    }

    public String generateToken(String email, List<String> authorities, String userId) {
        String roles = String.join(",", authorities);

        String token = Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProps.getExpirationTime()))
                .claim("email", email)
                .claim("authorities", roles)
                .claim("userId", userId)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        log.debug("Token generated for email: {}, userId: {}", email, userId);
        return token;
    }

    public String generateToken(String email, UserRole role) {
        return userClient.getAuthInfo(email)
                .map(userAuthInfo -> generateToken(email, List.of(role.name()), userAuthInfo.getId()))
                .block();
    }

    public String generateToken(Authentication auth) {
        Collection<? extends GrantedAuthority> authorities = auth.getAuthorities();
        String roles = populateAuthorities(authorities);

        return Jwts.builder()
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtProps.getExpirationTime()))
                .claim("email", auth.getName())
                .claim("authorities", roles)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public String getUserIdFromJwtToken(String token) {
        Claims claims = getClaims(token);
        Object userId = claims.get("userId");
        return userId != null ? String.valueOf(userId) : null;
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token);
            log.debug("Token is valid");
            return true;
        } catch (ExpiredJwtException e) {
            log.debug("Token expired: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.debug("Malformed token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.debug("Invalid signature: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            log.debug("Token validation error: {}", e.getMessage());
            return false;
        }
    }

    public String getEmailFromJwtToken(String token) {
        Claims claims = getClaims(token);
        return String.valueOf(claims.get("email"));
    }

    public List<String> getAuthorities(String token) {
        Claims claims = getClaims(token);
        String authoritiesStr = String.valueOf(claims.get("authorities"));
        return Arrays.asList(authoritiesStr.split(","));
    }

    public UserRole getRoleFromJwtToken(String token) {
        List<String> authorities = getAuthorities(token);
        if (!authorities.isEmpty()) {
            try {
                return UserRole.valueOf(authorities.get(0));
            } catch (IllegalArgumentException e) {
                log.warn("Unknown role: {}, defaulting to CUSTOMER", authorities.get(0));
                return UserRole.ROLE_CUSTOMER;
            }
        }
        return UserRole.ROLE_CUSTOMER;
    }

    public Date extractExpiration(String token) {
        return getClaims(token).getExpiration();
    }

    private String populateAuthorities(Collection<? extends GrantedAuthority> authorities) {
        return authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}

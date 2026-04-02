package com.ecommerce.platform.modules.auth.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import com.ecommerce.platform.modules.users.model.Role;
import com.ecommerce.platform.modules.users.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expirationMs;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-ms}") long expirationMs) {
        if (secret == null || secret.isBlank() || secret.length() < 32) {
            throw new IllegalStateException("app.jwt.secret must be set and at least 32 characters long");
        }
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        List<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .toList();

        return Jwts.builder()
                .setSubject(String.valueOf(user.getId()))
                .claim("email", user.getEmail())
                .claim("displayName", user.getDisplayName())
                .claim("roles", roles)
                .claim("userDiscountPercentage", user.getUserDiscountPercentage())
                .claim("userDiscountStartDate", user.getUserDiscountStartDate())
                .claim("userDiscountEndDate", user.getUserDiscountEndDate())
                .claim("enabled", user.isEnabled())
                .claim("accountNonLocked", user.isAccountNonLocked())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Jws<Claims> parseToken(String token) throws JwtException {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
    }

    public AuthenticatedUser toAuthenticatedUser(Claims claims) {
        return new AuthenticatedUser(
                Long.parseLong(claims.getSubject()),
                claims.get("email", String.class),
                claims.get("displayName", String.class),
                readRoles(claims),
                readBigDecimal(claims, "userDiscountPercentage"),
                readLocalDate(claims, "userDiscountStartDate"),
                readLocalDate(claims, "userDiscountEndDate"),
                readBoolean(claims, "enabled", true),
                readBoolean(claims, "accountNonLocked", true)
        );
    }

    private List<String> readRoles(Claims claims) {
        Object rolesClaim = claims.get("roles");
        if (rolesClaim instanceof List<?> rawRoles) {
            return rawRoles.stream().map(String::valueOf).toList();
        }
        return List.of();
    }

    private BigDecimal readBigDecimal(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value == null) {
            return BigDecimal.ZERO;
        }
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        if (value instanceof Number number) {
            return BigDecimal.valueOf(number.doubleValue());
        }
        return new BigDecimal(String.valueOf(value));
    }

    private LocalDate readLocalDate(Claims claims, String claimName) {
        Object value = claims.get(claimName);
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDate localDate) {
            return localDate;
        }
        if (value instanceof Map<?, ?> dateMap && dateMap.get("year") != null) {
            int year = Integer.parseInt(String.valueOf(dateMap.get("year")));
            int month = Integer.parseInt(String.valueOf(dateMap.get("monthValue")));
            int day = Integer.parseInt(String.valueOf(dateMap.get("dayOfMonth")));
            return LocalDate.of(year, month, day);
        }
        return LocalDate.parse(String.valueOf(value));
    }

    private boolean readBoolean(Claims claims, String claimName, boolean defaultValue) {
        Object value = claims.get(claimName);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean booleanValue) {
            return booleanValue;
        }
        return Boolean.parseBoolean(String.valueOf(value));
    }
}

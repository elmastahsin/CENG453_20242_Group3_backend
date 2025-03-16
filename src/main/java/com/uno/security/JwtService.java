package com.uno.security;

import com.uno.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtService {
    private final UserRepository userRepository;

    @Value("${user.jwt.key}")
    private String jwtSecret;

    @Value("${user.jwtExpirationMs}")
    private long jwtExpirationMs;

    @Value("${user.jwtRefreshExpirationMs}")
    private long refreshExpiration;

    public JwtService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // JWT access token oluşturma
    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", false);
        return createToken(claims, username, jwtExpirationMs);
    }

    // JWT token oluşturma
    private String createToken(Map<String, Object> claims, String userName, long expiration) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(key(), SignatureAlgorithm.HS512)
                .compact();
    }

    // Token geçerliliğini kontrol etme (roller kaldırıldı)
    public boolean validateToken(String authToken, UserDetails userDetails) {
        Boolean refresh = extractClaim(authToken, claims -> claims.get("refresh", Boolean.class));
        if (extractClaim(authToken, claims -> claims.get("refresh", Boolean.class))) {
            return false;
        }
        try {
            String username = extractUsername(authToken);
            Date expirationDate = extractExpiration(authToken);
            return userDetails.getUsername().equals(username) && !expirationDate.before(new Date());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    // Refresh token oluşturma (roller kaldırıldı)
    public String generateRefreshToken(String userName) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("refresh", true);
        return createToken(claims, userName, refreshExpiration);
    }

    // Refresh token geçerliliğini kontrol etme (roller kaldırıldı)
    public boolean validateRefreshToken(String token) {
        if (!extractClaim(token, claims -> claims.get("refresh", Boolean.class))) {
            return false;
        }
        try {
            Date expirationDate = extractExpiration(token);
            return expirationDate != null && !expirationDate.before(new Date());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }

        return false;
    }

    // Token'den kullanıcı adını çıkarma
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    // Token'den expiration tarihini çıkarma
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    // JWT'nin Authorization header'ında olup olmadığını kontrol etme
    public String parseJwt(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }

        return null;
    }

    // Token'deki tüm claim'leri çıkarma
    private Claims extractAllClaims(String token) {
        return Jwts
                .parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // Token'den istenilen claim'i çıkarma
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // JWT için kullanılan anahtarı alma
    private Key key() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }
}

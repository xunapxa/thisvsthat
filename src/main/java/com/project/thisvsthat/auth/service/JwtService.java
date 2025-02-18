package com.project.thisvsthat.auth.service;

import com.project.thisvsthat.common.entity.User;
import com.project.thisvsthat.common.enums.UserStatus;
import com.project.thisvsthat.common.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getEmail())  // 이메일을 subject로 저장
                .claim("userId", user.getUserId())  // 유저 ID 추가
                .claim("nickname", user.getNickname())  // 닉네임 추가
                .setIssuedAt(new Date())  // 발급 시간 설정
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))  // 만료 시간 설정
                .signWith(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)), SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8)))
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    public Long getUserIdFromToken(String token) {
        return getClaims(token).get("userId", Long.class);
    }
}

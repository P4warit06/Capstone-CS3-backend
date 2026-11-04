package com.example.demo.security;

import com.example.demo.entity.User;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    public static final String TOKEN_ISSUER = "planit-api";

    // กำหนด Secret Key สำหรับ Sign Token (แนะนำให้ใส่ใน application.properties แต่ใส่ default ไว้กัน error)
    @Value("${app.jwt.secret:MySuperSecretKeyForPlanITCapstone2026MustBeLongEnoughForHS256BitSecurity}")
    private String jwtSecret;

    // กำหนดอายุของ Token (เช่น 8 ชั่วโมง = 28,800,000 milliseconds)
    @Value("${app.jwt.expiration-ms:28800000}")
    private long jwtExpirationMs;

    public String generateToken(User user) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationMs);

        Key key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder()
                .setIssuer(TOKEN_ISSUER)
                .setSubject(user.getEmail()) // ใช้ Email เป็น Subject
                .claim("userId", user.getId()) // เก็บ userId ไว้ใน Claim
                .claim("studentId", user.getStudentId()) // เก็บ studentId ไว้ใน Claim
                .claim("role", user.getRole().name()) // เก็บ Role ไว้ใน Claim
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
}
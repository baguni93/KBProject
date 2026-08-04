package org.scoula.security.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
@PropertySource("classpath:/secret.properties")
public class JwtProcessor {

    @Value("${jwt.key}")
    private String secretKey; //Spring이 객체 생성 후 값을 주입해줌

    private Key key;

    // Access Token 유효시간: 1시간
    private static final long TOKEN_VALID_MILISECOND = 1000L * 60 * 60;

    // Refresh Token 유효시간: 14일
    private static final long REFRESH_TOKEN_VALID_MILISECOND =
            1000L * 60 * 60 * 24 * 14;

    // @Value 주입은 객체 생성(new) 이후에 일어난다.
    // 따라서 필드 선언 시점에는 secretKey가 아직 null이라
    // Keys.hmacShaKeyFor(secretKey.getBytes()) 를 바로 쓰면 NullPointerException 발생한다.
    // @PostConstruct는 객체 생성 + @Value 주입이 모두 끝난 후 자동으로 실행되므로
    // 이 시점에 key를 초기화하면 secretKey가 정상적으로 들어온 상태가 보장된다.
    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
    }

    // JWT 생성
    public String generateToken(String subject) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + TOKEN_VALID_MILISECOND))
                .signWith(key)
                .compact();
    }

    // Access Token 생성
    public String generateAccessToken(String subject, Long userId) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .claim("userId", userId)
                .claim("tokenType", "ACCESS")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + TOKEN_VALID_MILISECOND))
                .signWith(key)
                .compact();
    }

    // Refresh Token 생성
    public String generateRefreshToken(String subject, Long userId) {
        return Jwts.builder()
                .setHeaderParam("typ", "JWT")
                .setSubject(subject)
                .claim("userId", userId)
                .claim("tokenType", "REFRESH")
                .setIssuedAt(new Date())
                .setExpiration(new Date(new Date().getTime() + REFRESH_TOKEN_VALID_MILISECOND))
                .signWith(key)
                .compact();
    }

    // JWT Subject(username) 추출 - 해석 불가인 경우 예외 발생
    // 예외 ExpiredJwtException, UnsupportedJwtException, MalformedJwtException, SignatureException,
    // IllegalArgumentException
    public String getUsername(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getSubject();
    }

    // JWT 토큰 종류 추출
    public String getTokenType(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("tokenType", String.class);
    }

    // JWT 회원번호 추출
    public Long getUserId(String token) {
        Number userId = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("userId", Number.class);

        if (userId == null) {
            throw new IllegalArgumentException("토큰에 회원번호가 없습니다.");
        }

        return userId.longValue();
    }

    // JWT 검증(유효 기간 검증) - 해석 불가인 경우 예외 발생
    public boolean validateToken(String token) {
        Jws<Claims> claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token);
        return true;
    }

    // Access Token 유효시간 반환
    public long getAccessTokenValidSeconds() {
        return TOKEN_VALID_MILISECOND / 1000;
    }

    // Refresh Token 유효시간 반환
    public long getRefreshTokenValidSeconds() {
        return REFRESH_TOKEN_VALID_MILISECOND / 1000;
    }
}
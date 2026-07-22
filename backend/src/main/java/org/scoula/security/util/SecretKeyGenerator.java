package org.scoula.security.util;

import java.security.SecureRandom;
import java.util.Base64;

public class SecretKeyGenerator {
    public static void main(String[] args) {
        // 64바이트 길이의 시크릿 키 생성
        byte[] keyBytes = new byte[32];  // 512비트 = 64바이트
        SecureRandom random = new SecureRandom();
        random.nextBytes(keyBytes); // keyBytes 안에는 바이트 값들이 들어있음 바이트 배열을 application.properties나 secret.properties에 그대로 저장하려고 하면 깨지거나문제발생

        // Base64로 인코딩 // → A-Z, a-z, 0-9, +, / 로만 이루어진 안전한 문자열로 변환
        String encodedSecretKey = Base64.getEncoder().encodeToString(keyBytes);

        // 인코딩된 시크릿 키 출력
        System.out.println("Base64 Encoded Secret Key: " + encodedSecretKey);
    }
}
//Keys.hmacShaKeyFor()는 바이트 길이를 보고 알고리즘을 자동으로 판단하는데, 64바이트를 넣으면 내부적으로 HS512로 처리
// 알고리즘  | 해시 출력 크기 | 최소 키 길이
// ----------|---------------|---------------
// HS256     | 256비트        | 32바이트
// HS384     | 384비트        | 48바이트
// HS512     | 512비트        | 64바이트
//https://connect2id.com/products/nimbus-jose-jwt/examples/jwt-with-hmac
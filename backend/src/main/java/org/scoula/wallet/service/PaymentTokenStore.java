package org.scoula.wallet.service;

import org.scoula.wallet.dto.PaymentTokenDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [Redis TTL 기반 1회용 결제 토큰 저장소]
 * 
 * 실제 Redis 서버 연동 시:
 *   redisTemplate.opsForValue().set("PAY_TOKEN:" + token, dto, 180, TimeUnit.SECONDS);
 *   dto = (PaymentTokenDTO) redisTemplate.opsForValue().get("PAY_TOKEN:" + token);
 *   redisTemplate.delete("PAY_TOKEN:" + token);
 * 
 * 현재 구현:
 *   Redis 미설치 환경에서도 180초 TTL 자동 소멸 및 Single-use 검증이 완벽히 작동하도록 
 *   In-Memory ConcurrentHashMap + Scheduled TTL Eviction 백업 구조로 구현되어 있습니다.
 */
@Component
public class PaymentTokenStore {

    private static final long DEFAULT_TTL_SECONDS = 180L; // 3분 (180초) TTL

    // Redis Key-Value Store 역할을 하는 In-Memory TTL 저장소
    private final Map<String, TokenEntry> tokenStore = new ConcurrentHashMap<>();

    private static class TokenEntry {
        final PaymentTokenDTO tokenDTO;
        final long expireAtTimestamp;

        TokenEntry(PaymentTokenDTO tokenDTO, long expireAtTimestamp) {
            this.tokenDTO = tokenDTO;
            this.expireAtTimestamp = expireAtTimestamp;
        }

        boolean isExpired() {
            return System.currentTimeMillis() > expireAtTimestamp;
        }
    }

    // 1회용 결제 토큰 발급 (Redis SETEX key 180 value 대응)
    public PaymentTokenDTO createToken(Integer userId, Integer walletId, String tokenType) {
        String randomStr = UUID.randomUUID().toString().replace("-", "").substring(0, 12);
        String prefix = "BARCODE".equalsIgnoreCase(tokenType) ? "8804" : "KBQR";
        String tokenValue = prefix + randomStr;

        long now = System.currentTimeMillis();
        long expireAt = now + (DEFAULT_TTL_SECONDS * 1000);

        PaymentTokenDTO dto = PaymentTokenDTO.builder()
                .token(tokenValue)
                .tokenType(tokenType.toUpperCase())
                .userId(userId)
                .walletId(walletId)
                .expiresInSeconds(DEFAULT_TTL_SECONDS)
                .createdAt(new Date(now))
                .used(false)
                .build();

        /* Redis 연동 시: redisTemplate.opsForValue().set(tokenValue, dto, 180, TimeUnit.SECONDS); */
        tokenStore.put(tokenValue, new TokenEntry(dto, expireAt));
        return dto;
    }

    // 토큰 검증 및 1회 사용 즉시 소멸 (Single-use Consumed)
    public PaymentTokenDTO validateAndConsumeToken(String tokenValue) {
        /* Redis 연동 시: PaymentTokenDTO dto = redisTemplate.opsForValue().get(tokenValue); redisTemplate.delete(tokenValue); */
        TokenEntry entry = tokenStore.get(tokenValue);

        if (entry == null || entry.isExpired() || entry.tokenDTO.isUsed()) {
            if (entry != null) {
                tokenStore.remove(tokenValue);
            }
            throw new RuntimeException("유효하지 않거나 이미 만료된 1회용 결제 토큰입니다.");
        }

        // 1회 사용 후 저장소에서 즉시 삭제 (Single-use Security)
        entry.tokenDTO.setUsed(true);
        tokenStore.remove(tokenValue);

        return entry.tokenDTO;
    }

    // 토큰 정보 조회 (만료 여부 확인)
    public PaymentTokenDTO getToken(String tokenValue) {
        TokenEntry entry = tokenStore.get(tokenValue);
        if (entry == null || entry.isExpired()) {
            if (entry != null) tokenStore.remove(tokenValue);
            return null;
        }
        return entry.tokenDTO;
    }

    // 10초 주기 정기 만료 청소 (Redis Key Expiration / TTL Eviction 대응)
    @Scheduled(fixedRate = 10000)
    public void evictExpiredTokens() {
        tokenStore.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}

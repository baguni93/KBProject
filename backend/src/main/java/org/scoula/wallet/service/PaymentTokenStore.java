package org.scoula.wallet.service;

import org.scoula.wallet.dto.PaymentTokenDTO;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class PaymentTokenStore {

    private static final long DEFAULT_TTL_SECONDS = 60L;

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

        tokenStore.put(tokenValue, new TokenEntry(dto, expireAt));
        return dto;
    }

    public PaymentTokenDTO validateAndConsumeToken(String tokenValue) {
        TokenEntry entry = tokenStore.get(tokenValue);

        if (entry == null || entry.isExpired() || entry.tokenDTO.isUsed()) {
            if (entry != null) {
                tokenStore.remove(tokenValue);
            }
            throw new RuntimeException("유효하지 않거나 이미 만료된 1회용 결제 토큰입니다.");
        }

        entry.tokenDTO.setUsed(true);
        tokenStore.remove(tokenValue);

        return entry.tokenDTO;
    }

    public PaymentTokenDTO getToken(String tokenValue) {
        TokenEntry entry = tokenStore.get(tokenValue);
        if (entry == null || entry.isExpired()) {
            if (entry != null) tokenStore.remove(tokenValue);
            return null;
        }
        return entry.tokenDTO;
    }

    @Scheduled(fixedRate = 10000)
    public void evictExpiredTokens() {
        tokenStore.entrySet().removeIf(e -> e.getValue().isExpired());
    }
}

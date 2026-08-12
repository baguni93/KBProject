package org.scoula.cardpayment.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class KbCardCatalogRepository {

    // key: 카드 상품명, value: 로컬 저장 파일명
    private final Map<String, String> catalogMap = new ConcurrentHashMap<>();

    public void putCardInfo(String cardName, String fileName) {
        if (cardName != null && !cardName.trim().isEmpty() && fileName != null && !fileName.trim().isEmpty()) {
            catalogMap.put(cardName.trim(), fileName.trim());
        }
    }

    /**
     * 카드명 매핑 정밀도 개선:
     * 1. 완전 일치 검사
     * 2. 부분 일치 시 이름이 가장 길고 구체적인 카드명(예: 노리2 체크카드 > 노리 체크카드) 우선 반환
     */
    public String getImageUrlByCardName(String cardName) {
        if (cardName == null || cardName.trim().isEmpty()) {
            return "";
        }
        String cleanTarget = cardName.trim();

        // 1단계: 완전 일치 검사 (대소문자 무시)
        for (Map.Entry<String, String> entry : catalogMap.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(cleanTarget)) {
                return "/api/feeds/cardImage/" + entry.getValue();
            }
        }

        // 2단계: 부분 일치 검사 (가장 구체적이고 글자 수가 긴 매핑 키를 우선)
        String matchedFileName = catalogMap.entrySet().stream()
                .filter(entry -> cleanTarget.contains(entry.getKey()) || entry.getKey().contains(cleanTarget))
                .max(Comparator.comparingInt(entry -> entry.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse(null);

        if (matchedFileName != null) {
            return "/api/feeds/cardImage/" + matchedFileName;
        }

        return "";
    }

    public Map<String, String> getAllCatalog() {
        return catalogMap;
    }

    public int size() {
        return catalogMap.size();
    }

    public void clear() {
        catalogMap.clear();
    }
}
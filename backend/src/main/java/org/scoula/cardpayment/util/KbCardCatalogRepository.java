package org.scoula.cardpayment.util;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Component
public class KbCardCatalogRepository {

    // key: 카드 상품명, value: 로컬 저장 파일명 (예: 09252_img.png 또는 nori2.png)
    private final Map<String, String> catalogMap = new ConcurrentHashMap<>();

    public void putCardInfo(String cardName, String fileName) {
        if (cardName != null && !cardName.trim().isEmpty() && fileName != null && !fileName.trim().isEmpty()) {
            catalogMap.put(cardName.trim(), fileName.trim());
        }
    }

    public String getImageUrlByCardName(String cardName) {
        if (cardName == null || cardName.trim().isEmpty()) {
            return "";
        }
        String cleanTarget = cardName.trim();
        for (Map.Entry<String, String> entry : catalogMap.entrySet()) {
            if (entry.getKey().contains(cleanTarget) || cleanTarget.contains(entry.getKey())) {
                return "/api/feeds/cardImage/" + entry.getValue();
            }
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
package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users/{userId}/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // CARD-001 연결 카드 목록 조회
    @GetMapping
    public ResponseEntity<List<CardDTO>> getCards(@PathVariable Long userId) {
        return ResponseEntity.ok(cardService.getCards(userId));
    }

    // CARD-002 대표카드 설정
    @PatchMapping("/{linkedCardId}/represent")
    public ResponseEntity<Map<String, Object>> setRepresentCard(
            @PathVariable Long userId,
            @PathVariable Long linkedCardId
    ) {
        boolean result = cardService.setRepresentCard(userId, linkedCardId);
        Map<String, Object> response = new HashMap<>();

        response.put("success", result);
        response.put("message", "대표카드가 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // CARD-003 카드 연결 해제
    @DeleteMapping("/{linkedCardId}")
    public ResponseEntity<Map<String, Object>> disconnectCard(
            @PathVariable Long userId,
            @PathVariable Long linkedCardId
    ) {
        boolean result = cardService.disconnectCard(userId, linkedCardId);
        Map<String, Object> response = new HashMap<>();

        response.put("success", result);
        response.put("message", "카드 연결이 해제되었습니다.");

        return ResponseEntity.ok(response);
    }
}

package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.service.CardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // 실제 카드 BIN 앞자리(6~8자리)와 C:/upload/card/ 에 수집된 실제 53개 이미지 파일명을 1:1 유일하게 매핑하는 맵
    public static final Map<String, CardInfo> BIN_MAPPING_MAP = new HashMap<>();

    static {
        // [1] 씨티/코나아이/제휴 BIN
        BIN_MAPPING_MAP.put("539903", new CardInfo("KB국민 TBX 카드", "00218_img.jpg"));
        BIN_MAPPING_MAP.put("476020", new CardInfo("KB국민 VOLT UP EV 카드", "00236_img.png"));
        BIN_MAPPING_MAP.put("465524", new CardInfo("KB국민 So Young 체크카드", "01570_img.png"));
        BIN_MAPPING_MAP.put("455742", new CardInfo("KB국민 체크카드 (그래피티 디자인)", "01574_img.png"));
        BIN_MAPPING_MAP.put("440448", new CardInfo("KB국민 nori(노리) 체크카드", "01664_img.png"));
        BIN_MAPPING_MAP.put("520957", new CardInfo("KB국민 직장인보너스 체크카드", "01690_img.png"));
        BIN_MAPPING_MAP.put("536652", new CardInfo("KB국민 첵첵 체크카드", "01914_img.png"));
        BIN_MAPPING_MAP.put("949133", new CardInfo("KB국민 가온 올포인트 체크카드", "01998_img.png"));
        BIN_MAPPING_MAP.put("946044", new CardInfo("LG헬로비전 KB국민카드 II", "02083_img.png"));
        BIN_MAPPING_MAP.put("946045", new CardInfo("두산베어스 KB국민카드", "02219_img.jpg"));
        BIN_MAPPING_MAP.put("427239", new CardInfo("KB Youth Club 체크카드", "04124_img.png"));

        // [2] KB국민 신용/체크 BIN (36개)
        BIN_MAPPING_MAP.put("498761", new CardInfo("Liiv M Ⅱ 카드", "04241_img.png"));
        BIN_MAPPING_MAP.put("379541", new CardInfo("스카이패스 티타늄 카드", "04285_img.png"));
        BIN_MAPPING_MAP.put("466721", new CardInfo("T-economy KB국민카드", "04288_img.png"));
        BIN_MAPPING_MAP.put("493003", new CardInfo("SK 7mobile Ⅱ 카드", "04366_img.png"));
        BIN_MAPPING_MAP.put("421431", new CardInfo("가온플래티늄카드", "07964_img.jpg"));
        BIN_MAPPING_MAP.put("944541", new CardInfo("노리2 체크카드 (Play)", "07986_img.jpg"));
        BIN_MAPPING_MAP.put("944542", new CardInfo("노리2 체크카드 (Global)", "07998_img.png"));
        BIN_MAPPING_MAP.put("944543", new CardInfo("KB국민 다담카드", "09106_img.png"));
        BIN_MAPPING_MAP.put("944557", new CardInfo("KB국민 청춘대로 톡톡카드", "09123_img.png"));
        BIN_MAPPING_MAP.put("524028", new CardInfo("KB국민 탄탄대로 온리유 카드", "09125_img.png"));
        BIN_MAPPING_MAP.put("426504", new CardInfo("KB국민 청춘대로 카드", "09126_img.png"));
        BIN_MAPPING_MAP.put("544643", new CardInfo("KB국민 이지픽(Easy Pick) 카드", "09127_img.png"));
        BIN_MAPPING_MAP.put("444350", new CardInfo("KB국민 알파원(Alpha One) 카드", "09128_img.png"));
        BIN_MAPPING_MAP.put("457972", new CardInfo("KB국민 탄탄대로 올쇼핑 카드", "09129_img.jpg"));
        BIN_MAPPING_MAP.put("457973", new CardInfo("KB국민 마이 위시(My WE:SH) 카드", "09137_img.jpg"));
        BIN_MAPPING_MAP.put("540926", new CardInfo("KB국민 위시 올(WE:SH All) 카드", "09138_img.jpg"));
        BIN_MAPPING_MAP.put("540947", new CardInfo("KB국민 위시 디어(WE:SH Dear) 카드", "09139_img.jpg"));
        BIN_MAPPING_MAP.put("554959", new CardInfo("KB국민 탄탄대로 Biz 카드", "09152_img.png"));
        BIN_MAPPING_MAP.put("433290", new CardInfo("KB국민 청춘대로 티타늄 카드", "09162_img.png"));
        BIN_MAPPING_MAP.put("356910", new CardInfo("KB국민 이지온(Easy On) 카드", "09292_img.png"));
        BIN_MAPPING_MAP.put("356911", new CardInfo("KB국민 이지홈(Easy Home) 카드", "09297_img.png"));
        BIN_MAPPING_MAP.put("941012", new CardInfo("KB국민 이지스마트(Easy Smart) 카드", "09298_img.png"));
        BIN_MAPPING_MAP.put("941049", new CardInfo("KB국민 나라사랑카드", "09305_img.png"));
        BIN_MAPPING_MAP.put("941044", new CardInfo("KB국민 가온 파이낸스 카드", "09306_img.png"));
        BIN_MAPPING_MAP.put("494153", new CardInfo("KB국민 쇼핑앤쇼핑 카드", "09310_img.png"));
        BIN_MAPPING_MAP.put("433412", new CardInfo("KB국민 와이즈홈 카드", "09322_img.png"));
        BIN_MAPPING_MAP.put("943543", new CardInfo("KB국민 와이즈오토 카드", "09348_img.png"));
        BIN_MAPPING_MAP.put("949101", new CardInfo("KB국민 가온누리 카드", "09561_img.png"));
        BIN_MAPPING_MAP.put("943646", new CardInfo("KB국민 가온누리 체크카드", "09563_img.png"));
        BIN_MAPPING_MAP.put("544822", new CardInfo("KB국민 가온누리 쇼핑 카드", "09570_img.png"));
        BIN_MAPPING_MAP.put("546198", new CardInfo("KB국민 가온누리 비즈 카드", "09659_img.jpg"));
        BIN_MAPPING_MAP.put("545355", new CardInfo("KB국민 가온누리 플러스 카드", "09701_img.png"));
        BIN_MAPPING_MAP.put("554382", new CardInfo("KB국민 가온누리 트래블 카드", "09771_img.png"));
        BIN_MAPPING_MAP.put("623489", new CardInfo("KB국민 가온누리 스마트 카드", "09780_img.png"));
        BIN_MAPPING_MAP.put("626402", new CardInfo("KB국민 나라사랑체크카드", "79562_img.png"));
        BIN_MAPPING_MAP.put("949100", new CardInfo("KB국민 청춘대로 싱글 체크카드", "09790_img.png"));

        // [3] 선불/기타 BIN (6개)
        BIN_MAPPING_MAP.put("834000", new CardInfo("KB국민 청춘대로 오일 체크카드", "09792_img.png"));
        BIN_MAPPING_MAP.put("834024", new CardInfo("KB국민 청춘대로 톡톡 체크카드", "09800_img.png"));
        BIN_MAPPING_MAP.put("834026", new CardInfo("KB국민 청춘대로 아임인 체크카드", "09821_img.png"));
        BIN_MAPPING_MAP.put("949102", new CardInfo("KB국민 청춘대로 프리미엄 체크카드", "09922_img.png"));
        BIN_MAPPING_MAP.put("949139", new CardInfo("KB국민 청춘대로 티타늄 체크카드", "09924_img.png"));
        BIN_MAPPING_MAP.put("949144", new CardInfo("KB국민 나라사랑카드", "19565_img.png"));
    }

    // 공용 BIN 번호 기반 카드 정보 조회 API (사용자 ID 무관)
    @GetMapping("/api/cards/bin/{binNumber}")
    public ResponseEntity<CardInfo> getCardInfoByBin(@PathVariable("binNumber") String binNumber) {
        String cleanBin = binNumber.replaceAll("\\D", "");
        log.info("[BIN 매핑] 조회 요청된 BIN: {}", cleanBin);

        CardInfo info = null;
        if (cleanBin.length() >= 8) {
            info = BIN_MAPPING_MAP.get(cleanBin.substring(0, 8));
        }
        if (info == null && cleanBin.length() >= 6) {
            info = BIN_MAPPING_MAP.get(cleanBin.substring(0, 6));
        }

        if (info == null) {
            info = new CardInfo("KB국민 신용/체크카드", "09297_img.png");
        }

        return ResponseEntity.ok(info);
    }

    // CARD-001 연결 카드 목록 조회
    @GetMapping("/api/users/{userId}/cards")
    public ResponseEntity<List<CardDTO>> getCards(@PathVariable("userId") Long userId) {
        return ResponseEntity.ok(cardService.getCards(userId));
    }

    // CARD-002 대표카드 설정
    @PatchMapping("/api/users/{userId}/cards/{linkedCardId}/represent")
    public ResponseEntity<Map<String, Object>> setRepresentCard(
            @PathVariable("userId") Long userId,
            @PathVariable("linkedCardId") Long linkedCardId
    ) {
        boolean result = cardService.setRepresentCard(userId, linkedCardId);
        Map<String, Object> response = new HashMap<>();

        response.put("success", result);
        response.put("message", "대표카드가 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // CARD-003 카드 연결 해제
    @DeleteMapping("/api/users/{userId}/cards/{linkedCardId}")
    public ResponseEntity<Map<String, Object>> disconnectCard(
            @PathVariable("userId") Long userId,
            @PathVariable("linkedCardId") Long linkedCardId
    ) {
        boolean result = cardService.disconnectCard(userId, linkedCardId);
        Map<String, Object> response = new HashMap<>();

        response.put("success", result);
        response.put("message", "카드 연결이 해제되었습니다.");

        return ResponseEntity.ok(response);
    }

    public static class CardInfo {
        private String cardName;
        private String imageUrl;

        public CardInfo(String cardName, String imageUrl) {
            this.cardName = cardName;
            this.imageUrl = imageUrl;
        }

        public String getCardName() {
            return cardName;
        }

        public String getImageUrl() {
            return imageUrl;
        }
    }
}
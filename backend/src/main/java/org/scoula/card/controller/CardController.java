package org.scoula.card.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.domain.CardVO;
import org.scoula.card.dto.CardCustomCreateDTO;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.dto.CardMasterCreateDTO;
import org.scoula.card.service.CardService;
import org.scoula.security.account.domain.CustomUser;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.scoula.common.util.UploadFiles;
import org.scoula.common.util.UploadPathName;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Log4j2
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;
    private final org.scoula.card.mapper.CardMapper cardMapper;

    /** 커스텀 카드 전용 BIN 풀 (CardServiceImpl과 동일하게 유지) */
    private static final java.util.Set<String> CUSTOM_BIN_SET =
            new java.util.HashSet<>(java.util.Arrays.asList("421029", "463654", "484404", "463652"));

    // 실제 카드 BIN 앞자리(6~8자리)와 C:/upload/card/ 에 수집된 실제 53개 이미지 파일명을 1:1 유일하게 매핑하는 맵
    public static final Map<String, CardInfo> BIN_MAPPING_MAP = new HashMap<>();

    static {
        // Helper to normalize card names (collapse Unicode whitespace to single space)
        java.util.function.Function<String, String> normalize = name -> name.replace("\u00A0", " ").replace("\u202F", " ").replaceAll("[\\p{Z}\\s]+", " ").trim();
        // [1] 씨티/코나아이/제휴 BIN
        BIN_MAPPING_MAP.put("539903", new CardInfo(normalize.apply("KB국민 TBX 카드"), "00218_img.png"));
        BIN_MAPPING_MAP.put("476020", new CardInfo(normalize.apply("KB국민 VOLT UP EV 카드"), "00236_img.png"));
        BIN_MAPPING_MAP.put("465524", new CardInfo(normalize.apply("KB국민 So Young 체크카드"), "01570_img.png"));
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

        // 커스텀 BIN 풀에 속하는 경우에만 card_tbl에서 조회 (임의 번호로 잘못 매칭 방지)
        if (info == null && cleanBin.length() >= 6) {
            String prefix6 = cleanBin.substring(0, 6);
            boolean isCustomBin = CUSTOM_BIN_SET.contains(prefix6);
            if (isCustomBin) {
                try {
                    java.util.Map<String, String> row = cardMapper.findByBinPrefix(prefix6);
                    if (row != null && row.get("imageUrl") != null) {
                        info = new CardInfo(
                                row.getOrDefault("cardName", "커스텀 카드"),
                                row.get("imageUrl")
                        );
                        log.info("[BIN 매핑] 커스텀 card_tbl 조회 성공: BIN={}, img={}", prefix6, row.get("imageUrl"));
                    }
                } catch (Exception e) {
                    log.warn("[BIN 매핑] 커스텀 card_tbl 조회 실패: {}", e.getMessage());
                }
            }
        }

        // 완전히 인식되지 않는 BIN → imageUrl=null 반환 (프론트에서 이미지 미표시)
        if (info == null) {
            info = new CardInfo(null, null);
        }

        return ResponseEntity.ok(info);
    }

    // 카드 이미지 서빙 (1차: c:/upload/card/, 2차: c:/upload/customCard/)
    @GetMapping("/api/cards/image/{imageName}")
    public void getCardImage(@PathVariable String imageName, HttpServletResponse response) {
        try {
            File file = new File(UploadPathName.getCardPath() + imageName);
            if (!file.exists()) {
                file = new File(UploadPathName.getCustomCardPath() + imageName);
            }
            if (file.exists()) {
                UploadFiles.downloadImage(response, file);
            } else {
                response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            }
        } catch (Exception e) {
            log.warn("카드 이미지 서빙 중 에러 (무시하고 계속): {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    // CARD-001 연결 카드 목록 조회
    @GetMapping("/api/users/cards")
    public ResponseEntity<List<CardDTO>> getCards(@AuthenticationPrincipal CustomUser customUser) {
        Long userId = customUser.getUser().getUserId();
        return ResponseEntity.ok(cardService.getCards(userId));
    }

    // CARD-002 대표카드 설정
    @PatchMapping("/api/users/cards/{linkedCardId}/represent")
    public ResponseEntity<Map<String, Object>> setRepresentCard(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable("linkedCardId") Long linkedCardId
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean result = cardService.setRepresentCard(userId, linkedCardId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "대표카드가 변경되었습니다.");

        return ResponseEntity.ok(response);
    }

    // CARD-003 카드 연결 해제
    @DeleteMapping("/api/users/cards/{linkedCardId}")
    public ResponseEntity<Map<String, Object>> disconnectCard(
            @AuthenticationPrincipal CustomUser customUser,
            @PathVariable("linkedCardId") Long linkedCardId
    ) {
        Long userId = customUser.getUser().getUserId();
        boolean result = cardService.disconnectCard(userId, linkedCardId);

        Map<String, Object> response = new HashMap<>();
        response.put("success", result);
        response.put("message", "카드 연결이 해제되었습니다.");

        return ResponseEntity.ok(response);
    }

    /**
     * CARD-004 BIN 매핑 기반 카드 마스터 등록 (Admin/개발용)
     *
     * 목적:
     *  - 시연/테스트 전 DB에 KB국민카드를 사전 등록
     *  - cardName 으로 BIN_MAPPING_MAP 조회 → BIN + 이미지파일명 자동 결정
     *  - 카드번호(16자리), CVV, 유효기간, 비밀번호 서버 자동 생성
     *
     * POST /api/admin/cards
     * Body: { "accountId": 1, "cardName": "KB국민 TBX 카드" }
     */
    @PostMapping("/api/admin/cards")
    public ResponseEntity<Map<String, Object>> createCardMaster(
            @RequestBody CardMasterCreateDTO dto
    ) {
        CardVO created = cardService.createCardMaster(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cardCode", created.getCardCode());
        response.put("cardName", created.getCardName());
        response.put("cardNum", created.getCardNum());
        response.put("expiryDate", created.getExpiryDate());
        response.put("cardImgFileName", created.getCardImgFileName());
        response.put("message", "card_tbl 에 카드가 등록되었습니다.");
        return ResponseEntity.ok(response);
    }

    /**
     * CARD-005 커스텀 카드 등록용
     *  - BIN: 커스텀 전용 풀(421029, 463654, 484404, 463652) 에서 랜덤 선택
     *  - 카드번호(16자리), CVV, 유효기간 자동 생성
     * POST /api/admin/cards/custom
     * Body: { "cardName": "카드이름", "cardImgFileName": "custom_001.png", "cardPassword": "1234" }
     */
    @PostMapping("/api/admin/cards/custom")
    public ResponseEntity<Map<String, Object>> createCardMasterCustom(
            @RequestBody CardCustomCreateDTO dto
    ) {
        CardVO created = cardService.createCardMasterCustom(dto);
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("cardCode", created.getCardCode());
        response.put("cardName", created.getCardName());
        response.put("cardNum", created.getCardNum());
        response.put("expiryDate", created.getExpiryDate());
        response.put("cardImgFileName", created.getCardImgFileName());
        response.put("message", "커스텀 카드가 card_tbl 에 등록되었습니다.");
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
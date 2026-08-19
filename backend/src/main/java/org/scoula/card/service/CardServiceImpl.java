package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.controller.CardController;
import org.scoula.card.domain.CardVO;
import org.scoula.card.domain.LinkedCardVO;
import org.scoula.card.dto.CardCustomCreateDTO;
import org.scoula.card.dto.CardDTO;
import org.scoula.card.dto.CardMasterCreateDTO;
import org.scoula.card.mapper.CardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    /** 커스텀 카드 전용 BIN 풀 (KbCardBinRegistry 준수) */
    private static final List<String> CUSTOM_BINS = org.scoula.card.util.KbCardBinRegistry.CUSTOM_BINS;

    // ──────────────────────────────────────────────────────────────
    // 기존 기능
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<CardDTO> getCards(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("회원번호가 필요합니다.");
        }
        return cardMapper.findCardsByUserId(userId).stream()
                .map(CardDTO::of)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public boolean setRepresentCard(Long userId, Long linkedCardId) {
        LinkedCardVO card = cardMapper.findCardById(userId, linkedCardId);
        if (card == null) {
            throw new IllegalArgumentException("연결된 카드를 찾을 수 없습니다.");
        }
        if ("Y".equals(card.getRepresentYn())) return true;

        cardMapper.clearRepresentCard(userId);
        int result = cardMapper.setRepresentCard(userId, linkedCardId);
        if (result != 1) {
            throw new IllegalStateException("대표카드 설정에 실패했습니다.");
        }
        log.info("대표카드 변경 완료: userId={}, linkedCardId={}", userId, linkedCardId);
        return true;
    }

    @Override
    @Transactional
    public boolean disconnectCard(Long userId, Long linkedCardId) {
        LinkedCardVO card = cardMapper.findCardById(userId, linkedCardId);
        if (card == null) {
            throw new IllegalArgumentException("연결된 카드를 찾을 수 없습니다.");
        }

        int linkedCardCount = cardMapper.countLinkedCards(userId);
        if (linkedCardCount <= 1) {
            throw new IllegalArgumentException("마지막 카드는 연결 해제할 수 없습니다.");
        }

        LinkedCardVO nextRepresentCard = null;
        if ("Y".equals(card.getRepresentYn())) {
            nextRepresentCard = cardMapper.findAnotherLinkedCard(userId, linkedCardId);
            if (nextRepresentCard == null) {
                throw new IllegalStateException("대표카드로 설정할 다른 카드를 찾을 수 없습니다.");
            }
        }

        int result = cardMapper.deleteLinkedCard(userId, linkedCardId);
        if (result != 1) {
            throw new IllegalStateException("카드 연결 해제에 실패했습니다.");
        }

        if (nextRepresentCard != null) {
            int representResult = cardMapper.setRepresentCard(userId, nextRepresentCard.getLinkedCardId());
            if (representResult != 1) {
                throw new IllegalStateException("대표카드 재설정에 실패했습니다.");
            }
        }

        log.info("카드 연결 해제 완료: userId={}, linkedCardId={}", userId, linkedCardId);
        return true;
    }

    // ──────────────────────────────────────────────────────────────
    // 카드 마스터 등록 (크롤링 카탈로그 kb_card_product_tbl 우선 조회)
    // ──────────────────────────────────────────────────────────────

    /**
     * kb_card_product_tbl 에서 크롤링된 카드명으로 조회하여
     * 실제 다운로드된 이미지파일명과 매핑하고 card_tbl 에 INSERT.
     * 카드번호, CVV, 유효기간, 비밀번호는 서버에서 자동 생성.
     */
    @Override
    @Transactional
    public CardVO createCardMaster(CardMasterCreateDTO dto) {
        if (dto == null || dto.getCardName() == null) {
            throw new IllegalArgumentException("카드 이름이 입력되지 않았습니다.");
        }
        String rawInput = dto.getCardName().trim();
        String normalizedInput = rawInput.replaceAll("[\\p{Z}\\s]+", " ");
        String strippedInput = rawInput.replaceAll("[\\p{Z}\\s\\(\\):\\-_]+", "").toLowerCase();

        String foundCardName = rawInput;
        String foundImageFileName = null;
        String foundBin = "944501";

        // 1. 크롤링 DB (kb_card_product_tbl) 우선 검색
        List<Map<String, Object>> allProducts = cardMapper.findAllCardProducts();
        if (allProducts != null && !allProducts.isEmpty()) {
            for (Map<String, Object> prod : allProducts) {
                String pName = String.valueOf(prod.get("cardName"));
                String pImg = String.valueOf(prod.get("cardImage"));
                String pType = String.valueOf(prod.get("cardType"));

                String strippedProd = pName.replaceAll("[\\p{Z}\\s\\(\\):\\-_]+", "").toLowerCase();
                if (pName.equalsIgnoreCase(rawInput) || strippedProd.equals(strippedInput) || strippedProd.contains(strippedInput) || strippedInput.contains(strippedProd)) {
                    foundCardName = pName;
                    foundImageFileName = pImg;
                    foundBin = "CHECK".equalsIgnoreCase(pType) ? "944510" : "539910";
                    break;
                }
            }
        }

        // 2. 크롤링 DB 에 없으면 기존 BIN_MAPPING_MAP fallback 검색
        if (foundImageFileName == null) {
            for (Map.Entry<String, CardController.CardInfo> entry : CardController.BIN_MAPPING_MAP.entrySet()) {
                String strippedStored = entry.getValue().getCardName().replaceAll("[\\p{Z}\\s\\(\\):\\-_]+", "").toLowerCase();
                if (strippedStored.equals(strippedInput) || strippedStored.contains(strippedInput) || strippedInput.contains(strippedStored)) {
                    foundBin = entry.getKey();
                    foundImageFileName = entry.getValue().getImageUrl();
                    foundCardName = entry.getValue().getCardName();
                    break;
                }
            }
        }

        if (foundImageFileName == null) {
            foundImageFileName = "card_default.png";
        }

        // 2. 카드번호 생성: BIN(6자리) + 랜덤(10자리)
        String cardNum = generateCardNumber(foundBin);
        // 3. 유효기간 생성: 현재 기준 1~3년 후
        String expiry = generateExpiry();
        // 4. CVV 생성: SHA-256(카드번호 + 유효기간 + "101") → 3자리 숫자
        String cvv = generateCvv(cardNum, expiry);
        // 5. 비밀번호 랜덤 4자리
        String password = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));

        CardVO cardVO = CardVO.builder()
                .cardNum(cardNum)
                .expiryDate(expiry)
                .cvv(cvv)
                .cardPassword(password)
                .cardImgFileName(foundImageFileName != null ? foundImageFileName.replace(".jpg", ".png") : "card_default.png")
                .cardName(foundCardName)
                .build();

        int result = cardMapper.insertCard(cardVO);
        if (result != 1) {
            throw new IllegalStateException("card_tbl 삽입에 실패했습니다.");
        }
        log.info("크롤링 기반 카드 마스터 등록 완료: cardCode={}, cardName={}, image={}",
                cardVO.getCardCode(), cardVO.getCardName(), cardVO.getCardImgFileName());
        return cardVO;
    }

    // ──────────────────────────────────────────────────────────────
    // 커스텀 카드 등록
    // ──────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public CardVO createCardMasterCustom(CardCustomCreateDTO dto) {
        // 1. 커스텀 BIN 풀에서 랜덤 선택
        String bin = CUSTOM_BINS.get(ThreadLocalRandom.current().nextInt(CUSTOM_BINS.size()));

        // 2. 카드번호: 선택된 BIN(6자리) + 랜덤(10자리)
        String cardNum = generateCardNumber(bin);
        // 3. 유효기간 자동 생성
        String expiry = generateExpiry();
        // 4. CVV: SHA-256(카드번호 + 유효기간 + "101") → 3자리 숫자
        String cvv = generateCvv(cardNum, expiry);

        CardVO cardVO = CardVO.builder()
                .cardNum(cardNum)
                .expiryDate(expiry)
                .cvv(cvv)
                .cardPassword(dto.getCardPassword())
                .cardImgFileName(dto.getCardImgFileName())
                .cardName(dto.getCardName())
                .build();

        int result = cardMapper.insertCard(cardVO);
        if (result != 1) {
            throw new IllegalStateException("card_tbl 삽입에 실패했습니다.");
        }
        log.info("커스텀 카드 마스터 등록 완료: cardCode={}, cardName={}, BIN={}",
                cardVO.getCardCode(), cardVO.getCardName(), bin);
        return cardVO;
    }

    @Override
    @Transactional
    public List<CardVO> createAllCardMasters() {
        List<CardVO> resultList = new java.util.ArrayList<>();
        List<Map<String, Object>> allProducts = cardMapper.findAllCardProducts();

        if (allProducts != null && !allProducts.isEmpty()) {
            int checkIdx = 0;
            int creditIdx = 0;

            for (Map<String, Object> prod : allProducts) {
                String cardName = String.valueOf(prod.get("cardName"));
                String cardImage = String.valueOf(prod.get("cardImage"));
                String cardType = String.valueOf(prod.get("cardType"));

                boolean isCheck = "CHECK".equalsIgnoreCase(cardType)
                        || (cardName != null && cardName.contains("체크"));

                // 공식 299개 BIN 풀에서 신용/체크 타입별 1:1 고유 BIN 할당
                String binPrefix = isCheck
                        ? org.scoula.card.util.KbCardBinRegistry.getBinForCard(checkIdx++, "CHECK", cardName)
                        : org.scoula.card.util.KbCardBinRegistry.getBinForCard(creditIdx++, "CREDIT", cardName);

                String cardNum = generateCardNumber(binPrefix);
                String expiry = generateExpiry();
                String cvv = generateCvv(cardNum, expiry);
                String password = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
                String imgName = (cardImage != null && !cardImage.isBlank() && !"null".equals(cardImage))
                        ? cardImage.replace(".jpg", ".png")
                        : "card_default.png";

                CardVO cardVO = CardVO.builder()
                        .cardNum(cardNum)
                        .expiryDate(expiry)
                        .cvv(cvv)
                        .cardPassword(password)
                        .cardImgFileName(imgName)
                        .cardName(cardName)
                        .build();

                int insertRes = cardMapper.insertCard(cardVO);
                if (insertRes == 1) {
                    resultList.add(cardVO);
                }
            }
            log.info("크롤링 카탈로그(kb_card_product_tbl) 기반 공식 BIN 1:1 매핑 카드 마스터 등록 완료: 총 {}건", resultList.size());
        } else {
            // fallback: BIN_MAPPING_MAP
            for (Map.Entry<String, CardController.CardInfo> entry : CardController.BIN_MAPPING_MAP.entrySet()) {
                String bin = entry.getKey();
                CardController.CardInfo info = entry.getValue();

                String cardNum = generateCardNumber(bin);
                String expiry = generateExpiry();
                String cvv = generateCvv(cardNum, expiry);
                String password = String.format("%04d", ThreadLocalRandom.current().nextInt(0, 10000));
                String imgName = info.getImageUrl() != null ? info.getImageUrl().replace(".jpg", ".png") : "card_default.png";

                CardVO cardVO = CardVO.builder()
                        .cardNum(cardNum)
                        .expiryDate(expiry)
                        .cvv(cvv)
                        .cardPassword(password)
                        .cardImgFileName(imgName)
                        .cardName(info.getCardName())
                        .build();

                int insertRes = cardMapper.insertCard(cardVO);
                if (insertRes == 1) {
                    resultList.add(cardVO);
                }
            }
            log.info("기본 BIN_MAPPING_MAP 기반 카드 마스터 일괄 등록 완료: 총 {}건", resultList.size());
        }

        return resultList;
    }

    // ──────────────────────────────────────────────────────────────
    // 헬퍼 메서드
    // ──────────────────────────────────────────────────────────────


    /** BIN 6자리 + 랜덤 10자리 = 16자리 카드번호 생성 */
    private String generateCardNumber(String bin) {
        StringBuilder sb = new StringBuilder(bin);
        while (sb.length() < 16) {
            sb.append(ThreadLocalRandom.current().nextInt(10));
        }
        return sb.toString();
    }

    /** 현재 기준 1~3년 후 유효기간 (MM/YY 형식) */
    private String generateExpiry() {
        YearMonth now = YearMonth.now();
        int yearsToAdd = 1 + ThreadLocalRandom.current().nextInt(3);
        YearMonth expiry = now.plusYears(yearsToAdd);
        return String.format("%02d/%02d", expiry.getMonthValue(), expiry.getYear() % 100);
    }

    /**
     * CVV 생성
     * 공식: Encrypt_CVK(카드번호 + 유효기간 + 서비스코드) → 3자리 숫자
     * 시연 환경: SHA-256 해시로 플레이스홀더 구현 (실 운영에서는 HSM 사용)
     */
    private String generateCvv(String cardNum, String expiryDate) {
        String raw = cardNum + expiryDate + "101"; // 서비스 코드 101
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(raw.getBytes());
            StringBuilder numeric = new StringBuilder();
            for (byte b : hash) {
                numeric.append(Math.abs(b % 10));
                if (numeric.length() >= 3) break;
            }
            return numeric.substring(0, 3);
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 이 없을 경우 폴백 (실제로는 발생하지 않음)
            return String.format("%03d", ThreadLocalRandom.current().nextInt(0, 1000));
        }
    }
}

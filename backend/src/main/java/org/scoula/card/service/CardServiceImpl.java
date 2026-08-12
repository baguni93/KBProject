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

    /** 커스텀 카드 전용 BIN 풀 (기존 BIN_MAPPING_MAP 과 겹치지 않음) */
    private static final String[] CUSTOM_BINS = {"421029", "463654", "484404", "463652"};

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
    // 카드 마스터 등록 (BIN 매핑 기반)
    // ──────────────────────────────────────────────────────────────

    /**
     * BIN_MAPPING_MAP 에서 cardName 으로 역방향 조회하여
     * BIN 6자리 + 이미지파일명을 자동으로 결정하고 card_tbl 에 INSERT.
     * 카드번호, CVV, 유효기간, 비밀번호는 서버에서 자동 생성.
     */
    @Override
    @Transactional
    public CardVO createCardMaster(CardMasterCreateDTO dto) {
        // 1. 카드명 정규화: 모든 공백(일반, 비표준) 을 단일 스페이스로 변환
        String normalizedInput = dto.getCardName().replaceAll("[\\p{Z}\\s]+", " ").trim();
        String foundBin = null;
        String foundImageFileName = null;
        for (Map.Entry<String, CardController.CardInfo> entry : CardController.BIN_MAPPING_MAP.entrySet()) {
            String normalizedStored = entry.getValue().getCardName().replaceAll("[\\p{Z}\\s]+", " ").trim();
            if (normalizedStored.equals(normalizedInput)) {
                foundBin = entry.getKey();
                foundImageFileName = entry.getValue().getImageUrl();
                break;
            }
        }
        if (foundBin == null) {
            throw new IllegalArgumentException(
                    "BIN 매핑에서 찾을 수 없는 카드명입니다: " + dto.getCardName());
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
                .cardImgFileName(foundImageFileName)
                .cardName(dto.getCardName())
                .build();

        int result = cardMapper.insertCard(cardVO);
        if (result != 1) {
            throw new IllegalStateException("card_tbl 삽입에 실패했습니다.");
        }
        log.info("BIN 기반 카드 마스터 등록 완료: cardCode={}, cardName={}, BIN={}",
                cardVO.getCardCode(), cardVO.getCardName(), foundBin);
        return cardVO;
    }

    // ──────────────────────────────────────────────────────────────
    // 커스텀 카드 마스터 등록
    // ──────────────────────────────────────────────────────────────

    /**
     * 디자인팀이 만든 커스텀 카드를 card_tbl 에 등록.
     * - BIN: 커스텀 전용 풀(421029, 463654, 484404, 463652) 에서 랜덤 선택
     * - 카드번호(16자리), CVV, 유효기간 자동 생성
     * - 카드명, 이미지파일명, 비밀번호는 입력 받음
     */
    @Override
    @Transactional
    public CardVO createCardMasterCustom(CardCustomCreateDTO dto) {
        // 1. 커스텀 BIN 풀에서 랜덤 선택
        String bin = CUSTOM_BINS[ThreadLocalRandom.current().nextInt(CUSTOM_BINS.length)];

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

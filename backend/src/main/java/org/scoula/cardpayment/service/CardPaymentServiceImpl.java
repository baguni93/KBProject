package org.scoula.cardpayment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.cardpayment.dto.CardAgreementDTO;
import org.scoula.cardpayment.dto.CardBinResponseDTO;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.CardStatusResponseDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;
import org.scoula.cardpayment.mapper.CardPaymentMapper;
import org.scoula.cardpayment.util.KbCardCatalogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardPaymentServiceImpl implements CardPaymentService {

    private final CardPaymentMapper cardPaymentMapper;
    private final KbCardCatalogRepository catalogRepository;

    private final Map<String, CardBinResponseDTO> binMemoryCache = new ConcurrentHashMap<>();

    @Override
    public PrimaryCardResponseDTO getPrimaryCard(Integer userId) {
        log.info("대표 카드 조회 - 회원 ID: {}", userId);
        PrimaryCardResponseDTO primaryCard = cardPaymentMapper.getPrimaryCardByUserId(userId);
        if (primaryCard == null) {
            log.warn("대표 카드가 존재하지 않거나 지정되지 않음 - 회원 ID: {}", userId);
            return PrimaryCardResponseDTO.builder()
                    .userId(userId)
                    .representYn("N")
                    .paymentStatus("NO_PRIMARY_CARD")
                    .message("대표 카드가 지정되지 않았습니다.")
                    .build();
        }
        return primaryCard;
    }

    @Override
    public CardStatusResponseDTO getCardStatus(Integer userId) {
        log.info("카드 등록 상태 및 가이드 조회 - 회원 ID: {}", userId);
        int totalCards = cardPaymentMapper.countCardsByUserId(userId);
        int primaryCards = cardPaymentMapper.countPrimaryCardsByUserId(userId);

        boolean hasCard = totalCards > 0;
        boolean hasPrimary = primaryCards > 0;

        PrimaryCardResponseDTO primary = hasPrimary ? cardPaymentMapper.getPrimaryCardByUserId(userId) : null;
        Integer primaryId = (primary != null) ? primary.getCardId() : null;

        String statusCode;
        String guideMessage;

        if (!hasCard) {
            statusCode = "NO_CARD";
            guideMessage = "등록된 결제 카드가 없습니다. 대표 카드를 등록하여 간편 결제를 시작하세요.";
        } else if (!hasPrimary) {
            statusCode = "NEED_PRIMARY_CARD";
            guideMessage = "등록된 카드는 있으나 대표 카드가 지정되지 않았습니다. 대표 카드를 지정해 주세요.";
        } else {
            statusCode = "PRIMARY_CARD_SET";
            guideMessage = "대표 카드가 정상 설정되어 무선/간편 결제가 가능합니다.";
        }

        return CardStatusResponseDTO.builder()
                .userId(userId)
                .hasRegisteredCard(hasCard)
                .hasPrimaryCard(hasPrimary)
                .registeredCardCount(totalCards)
                .primaryCardId(primaryId)
                .statusCode(statusCode)
                .guideMessage(guideMessage)
                .build();
    }

    @Override
    @Transactional
    public PrimaryCardResponseDTO registerCard(CardRegisterDTO cardRegisterDTO) {
        log.info("카드 등록 요청: {}", cardRegisterDTO);

        Integer userId = cardRegisterDTO.getUserId();
        int existingCards = cardPaymentMapper.countCardsByUserId(userId);

        if (cardRegisterDTO.getCardNum() == null || cardRegisterDTO.getCardNum().trim().isEmpty()) {
            cardRegisterDTO.setCardNum("9410-1234-5678-9999");
        } else {
            // 하이픈/공백 등 비숫자 문자 제거 (DB에는 숫자만 저장됨)
            cardRegisterDTO.setCardNum(cardRegisterDTO.getCardNum().replaceAll("[^0-9]", ""));
        }
        if (cardRegisterDTO.getExpiryDate() == null || cardRegisterDTO.getExpiryDate().trim().isEmpty()) {
            cardRegisterDTO.setExpiryDate("12/28");
        } else {
            String exp = cardRegisterDTO.getExpiryDate().replaceAll("[^0-9/]", "");
            if (exp.contains("/")) {
                String[] parts = exp.split("/");
                String mm = parts[0];
                String yy = parts.length > 1 ? parts[1] : "28";
                if (yy.length() == 4) yy = yy.substring(2);
                if (mm.length() == 1) mm = "0" + mm;
                exp = mm + "/" + yy;
            } else if (exp.length() == 4) {
                exp = exp.substring(0, 2) + "/" + exp.substring(2);
            }
            if (exp.length() > 5) exp = exp.substring(0, 5);
            cardRegisterDTO.setExpiryDate(exp);
        }
        if (cardRegisterDTO.getCvv() == null || cardRegisterDTO.getCvv().trim().isEmpty()) {
            cardRegisterDTO.setCvv("777");
        } else if (cardRegisterDTO.getCvv().length() > 4) {
            cardRegisterDTO.setCvv(cardRegisterDTO.getCvv().substring(0, 4));
        }
        if (cardRegisterDTO.getCardPassword() == null || cardRegisterDTO.getCardPassword().trim().isEmpty()) {
            cardRegisterDTO.setCardPassword("1234");
        }

        if (existingCards == 0 || "Y".equalsIgnoreCase(cardRegisterDTO.getRepresentYn())) {
            cardRegisterDTO.setRepresentYn("Y");
            cardPaymentMapper.resetPrimaryCardByUserId(userId);
            cardPaymentMapper.resetLinkedPrimaryCardByUserId(userId);
        } else {
            cardRegisterDTO.setRepresentYn("N");
        }

        // Validate that the card exists in card_tbl
        Integer validatedCardCode = cardPaymentMapper.validateCard(cardRegisterDTO);
        if (validatedCardCode == null) {
            throw new IllegalArgumentException("카드 정보를 찾을 수 없습니다.");
        }
        cardRegisterDTO.setCardCode(validatedCardCode);

        // 기존 이미지명 조회 로직 유지
        if (cardRegisterDTO.getCardImageName() == null || cardRegisterDTO.getCardImageName().trim().isEmpty()) {
            String foundImg = catalogRepository.getImageUrlByCardName(cardRegisterDTO.getCardName());
            cardRegisterDTO.setCardImageName(foundImg);
        }



        try {
            cardPaymentMapper.insertLinkedCard(cardRegisterDTO);
        } catch (Exception e) {
            log.warn("linked_card_tbl 동기화 생성 경고: {}", e.getMessage());
        }

        return getPrimaryCard(userId);
    }

    @Override
    @Transactional
    public boolean saveCardAgreements(CardAgreementDTO cardAgreementDTO) {
        log.info("카드 결제 약관 동의 요청: {}", cardAgreementDTO);
        if (cardAgreementDTO.getAgreementIds() == null || cardAgreementDTO.getAgreementIds().isEmpty()) {
            return false;
        }

        String agreed = (cardAgreementDTO.getAgreedYn() != null) ? cardAgreementDTO.getAgreedYn() : "Y";
        for (Integer agreementId : cardAgreementDTO.getAgreementIds()) {
            cardPaymentMapper.insertUserAgreement(cardAgreementDTO.getUserId(), agreementId, agreed);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean setPrimaryCard(Integer cardId, Integer userId) {
        log.info("대표 카드 변경 요청 - CardID: {}, UserID: {}", cardId, userId);
        cardPaymentMapper.resetPrimaryCardByUserId(userId);
        int rows = cardPaymentMapper.setPrimaryCard(cardId, userId);
        return rows > 0;
    }

    @Override
    public CardBinResponseDTO getAutoFetchedCardInfo(String binNumber) {
        log.info("자동 수집 카탈로그 기반 BIN 조회 - BIN: {}", binNumber);

        if (binMemoryCache.containsKey(binNumber)) {
            return binMemoryCache.get(binNumber);
        }

        String cleanBin = binNumber.replaceAll("\\D", "");
        org.scoula.card.controller.CardController.CardInfo info = null;
        if (cleanBin.length() >= 8) {
            info = org.scoula.card.controller.CardController.BIN_MAPPING_MAP.get(cleanBin.substring(0, 8));
        }
        if (info == null && cleanBin.length() >= 6) {
            info = org.scoula.card.controller.CardController.BIN_MAPPING_MAP.get(cleanBin.substring(0, 6));
        }

        String cardName = (info != null) ? info.getCardName() : "KB국민 신용/체크카드";
        String imageName = (info != null) ? info.getImageUrl() : "09297_img.png";

        CardBinResponseDTO responseDTO = CardBinResponseDTO.builder()
                .binNumber(binNumber)
                .cardName(cardName)
                .imageUrl(imageName)
                .build();

        binMemoryCache.put(binNumber, responseDTO);
        return responseDTO;
    }
}
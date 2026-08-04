package org.scoula.card.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.card.dto.CardAgreementDTO;
import org.scoula.card.dto.CardRegisterDTO;
import org.scoula.card.dto.CardStatusResponseDTO;
import org.scoula.card.dto.PrimaryCardResponseDTO;
import org.scoula.card.mapper.CardMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class CardServiceImpl implements CardService {

    private final CardMapper cardMapper;

    @Override
    public PrimaryCardResponseDTO getPrimaryCard(Integer userId) {
        log.info("대표 카드 조회 - 회원 ID: {}", userId);
        PrimaryCardResponseDTO primaryCard = cardMapper.getPrimaryCardByUserId(userId);
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
        int totalCards = cardMapper.countCardsByUserId(userId);
        int primaryCards = cardMapper.countPrimaryCardsByUserId(userId);

        boolean hasCard = totalCards > 0;
        boolean hasPrimary = primaryCards > 0;

        PrimaryCardResponseDTO primary = hasPrimary ? cardMapper.getPrimaryCardByUserId(userId) : null;
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
        int existingCards = cardMapper.countCardsByUserId(userId);

        if (cardRegisterDTO.getCardNum() == null || cardRegisterDTO.getCardNum().trim().isEmpty()) {
            cardRegisterDTO.setCardNum("9410-1234-5678-9999");
        }
        if (cardRegisterDTO.getExpiryDate() == null || cardRegisterDTO.getExpiryDate().trim().isEmpty()) {
            cardRegisterDTO.setExpiryDate("12/28");
        }
        if (cardRegisterDTO.getCvv() == null || cardRegisterDTO.getCvv().trim().isEmpty()) {
            cardRegisterDTO.setCvv("777");
        }
        if (cardRegisterDTO.getCardPassword() == null || cardRegisterDTO.getCardPassword().trim().isEmpty()) {
            cardRegisterDTO.setCardPassword("1234");
        }

        // 첫 카드 등록이거나 representYn이 'Y'인 경우
        if (existingCards == 0 || "Y".equalsIgnoreCase(cardRegisterDTO.getRepresentYn())) {
            cardRegisterDTO.setRepresentYn("Y");
            cardMapper.resetPrimaryCardByUserId(userId);
        } else {
            cardRegisterDTO.setRepresentYn("N");
        }

        cardMapper.insertCard(cardRegisterDTO);
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
            cardMapper.insertUserAgreement(cardAgreementDTO.getUserId(), agreementId, agreed);
        }
        return true;
    }

    @Override
    @Transactional
    public boolean setPrimaryCard(Integer cardId, Integer userId) {
        log.info("대표 카드 변경 요청 - CardID: {}, UserID: {}", cardId, userId);
        cardMapper.resetPrimaryCardByUserId(userId);
        int rows = cardMapper.setPrimaryCard(cardId, userId);
        return rows > 0;
    }
}

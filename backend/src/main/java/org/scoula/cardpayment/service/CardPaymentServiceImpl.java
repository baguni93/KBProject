package org.scoula.cardpayment.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.analysis.dto.MerchantCategoryClassificationResultDTO;
import org.scoula.analysis.service.MerchantCategoryService;
import org.scoula.card.controller.CardController;
import org.scoula.cardpayment.dto.CardAgreementDTO;
import org.scoula.cardpayment.dto.CardBinResponseDTO;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.CardStatusResponseDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;
import org.scoula.cardpayment.mapper.CardPaymentMapper;
import org.scoula.cardpayment.util.KbCardCatalogRepository;
import org.scoula.common.util.Enum;
import org.scoula.task.service.TaskEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Log4j2
@Service
@RequiredArgsConstructor
public class CardPaymentServiceImpl implements CardPaymentService {

    private final CardPaymentMapper cardPaymentMapper;
    private final org.scoula.card.mapper.CardMapper cardMapper;
    private final KbCardCatalogRepository catalogRepository;
    private final TaskEventService taskEventService;
    private final org.scoula.notification.service.NotificationService notificationService;
    private final org.scoula.pointwallet.service.RandomBoxService randomBoxService;
    // 자동 AI 카테고리 분류 서비스 연결완료
    private final MerchantCategoryService merchantCategoryService;

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
            // card_tbl에 카드가 사전 등록되어 있지 않은 경우, 즉시 자동 마스터 생성하여 등록 지원
            String cleanNum = cardRegisterDTO.getCardNum().replaceAll("\\D", "");
            String bin6 = cleanNum.length() >= 6 ? cleanNum.substring(0, 6) : "941012";
            String bin8 = cleanNum.length() >= 8 ? cleanNum.substring(0, 8) : bin6;

            String cardName = cardRegisterDTO.getCardName();
            String cardImg = cardRegisterDTO.getCardImageName();

            // 1) BIN 매핑에서 이름/이미지 조회
            if (CardController.BIN_MAPPING_MAP.containsKey(bin8)) {
                CardController.CardInfo info = CardController.BIN_MAPPING_MAP.get(bin8);
                if (cardName == null || cardName.trim().isEmpty()) cardName = info.getCardName();
                if (cardImg == null || cardImg.trim().isEmpty()) cardImg = info.getImageUrl();
            } else if (CardController.BIN_MAPPING_MAP.containsKey(bin6)) {
                CardController.CardInfo info = CardController.BIN_MAPPING_MAP.get(bin6);
                if (cardName == null || cardName.trim().isEmpty()) cardName = info.getCardName();
                if (cardImg == null || cardImg.trim().isEmpty()) cardImg = info.getImageUrl();
            }

            if (cardName == null || cardName.trim().isEmpty()) {
                cardName = "KB국민카드";
            }
            if (cardImg == null || cardImg.trim().isEmpty()) {
                cardImg = "card_default.png";
            }

            String rawPw = cardRegisterDTO.getCardPassword() != null ? cardRegisterDTO.getCardPassword() : "1234";
            String hashedPw = hashCardPassword(rawPw);

            org.scoula.card.domain.CardVO newCard = org.scoula.card.domain.CardVO.builder()
                    .cardNum(cardRegisterDTO.getCardNum())
                    .expiryDate(cardRegisterDTO.getExpiryDate())
                    .cvv(cardRegisterDTO.getCvv())
                    .cardPassword(hashedPw)
                    .cardName(cardName)
                    .cardImgFileName(cardImg)
                    .build();

            cardMapper.insertCard(newCard);
            validatedCardCode = newCard.getCardCode();
            log.info("신규 카드 마스터 자동 생성 완료: cardCode={}, cardName={}, cardImg={}", validatedCardCode, cardName, cardImg);
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

    private String hashCardPassword(String raw) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(raw.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (Exception e) {
            return raw;
        }
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

        // 1. catalogRepository 및 BIN_MAPPING_MAP 조회
        org.scoula.card.controller.CardController.CardInfo info = null;
        if (cleanBin.length() >= 8) {
            info = org.scoula.card.controller.CardController.BIN_MAPPING_MAP.get(cleanBin.substring(0, 8));
        }
        if (info == null && cleanBin.length() >= 6) {
            info = org.scoula.card.controller.CardController.BIN_MAPPING_MAP.get(cleanBin.substring(0, 6));
        }

        String cardName = (info != null) ? info.getCardName() : "KB국민 신용/체크카드";
        String imageName = (info != null) ? info.getImageUrl() : "09297_img.png";

        // 2. 크롤링 카탈로그에서 더 최신의 정확한 이미지 파일명이 있으면 갱신
        if (catalogRepository != null && cardName != null) {
            String catalogImg = catalogRepository.getImageUrlByCardName(cardName);
            if (catalogImg != null && !catalogImg.isBlank()) {
                String fileOnly = catalogImg.substring(catalogImg.lastIndexOf('/') + 1);
                if (!fileOnly.isBlank()) {
                    imageName = fileOnly;
                }
            }
        }

        CardBinResponseDTO responseDTO = CardBinResponseDTO.builder()
                .binNumber(binNumber)
                .cardName(cardName)
                .imageUrl(imageName)
                .build();

        binMemoryCache.put(binNumber, responseDTO);
        return responseDTO;
    }

    @Override
    @Transactional
    public org.scoula.cardpayment.dto.CardTransactionResponseDTO createPendingTransaction(org.scoula.cardpayment.dto.CardTransactionRequestDTO requestDTO) {
        log.info("1단계: 카드 결제 대기(PENDING) 레코드 생성 - LinkedCardID: {}", requestDTO.getLinkedCardId());

        Integer linkedCardId = requestDTO.getLinkedCardId();
        if (linkedCardId == null || linkedCardId <= 0) {
            linkedCardId = 1; // 기본 연동 카드 ID
        }

        org.scoula.cardpayment.domain.CardTransactionDetailVO vo = org.scoula.cardpayment.domain.CardTransactionDetailVO.builder()
                .linkedCardId(linkedCardId)
                .status("PENDING")
                .build();

        cardPaymentMapper.insertCardTransactionDetail(vo);

        log.info("결제 대기 생성 완료 - CardTransactionID: {}", vo.getCardTransactionId());

        return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                .cardTransactionId(vo.getCardTransactionId())
                .linkedCardId(linkedCardId)
                .status("PENDING")
                .createdAt(vo.getCreatedAt())
                .message("결제 대기 상태로 등록되었습니다.")
                .build();
    }

    // 1. 카드 결제 승인 (지갑 잔액 차감 없이 무조건 SUCCESS 기록 보존)
    @Override
    @Transactional
    public org.scoula.cardpayment.dto.CardTransactionResponseDTO approveTransaction(org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO) {
        log.info("카드 결제 승인 요청 (지갑 잔액 차감 X) - DTO: {}", approveDTO);

        Integer targetUserId = approveDTO.getUserId();
        if (targetUserId == null || targetUserId <= 0) {
            targetUserId = 1;
        }

        Long cardTxId = approveDTO.getCardTransactionId();
        org.scoula.cardpayment.domain.CardTransactionDetailVO detailVO = null;

        if (cardTxId != null && cardTxId > 0) {
            detailVO = cardPaymentMapper.getCardTransactionDetailById(cardTxId);
        } else {
            detailVO = cardPaymentMapper.getPendingTransactionByUserId(targetUserId);
        }

        if (detailVO == null || !"PENDING".equalsIgnoreCase(detailVO.getStatus())) {
            log.warn("카드 결제 승인 실패: 유효한 결제 대기(PENDING) 건이 없습니다. UserID: {}", targetUserId);
            return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                    .status("FAILED")
                    .message("현재 활성화된 결제 대기 상태가 아닙니다. 카드 결제 화면을 열고 다시 시도해 주세요.")
                    .build();
        }

        cardTxId = detailVO.getCardTransactionId();

        String merchantName = (approveDTO.getMerchantName() != null && !approveDTO.getMerchantName().isBlank())
                ? approveDTO.getMerchantName().trim() : "스타벅스";
        Integer amount = (approveDTO.getAmount() != null && approveDTO.getAmount() > 0)
                ? approveDTO.getAmount() : 10000;

        // 가맹점 수신 회원 ID(receive_id) 조회
        Integer merchantAccountId = approveDTO.getMerchantAccountId();
        if (merchantAccountId == null) {
            merchantAccountId = cardPaymentMapper.getMerchantAccountIdByName(merchantName);
            if (merchantAccountId == null) merchantAccountId = 99;
        }
        Integer receiveId = cardPaymentMapper.getMerchantUserIdByAccountId(merchantAccountId);

        // 결제 가맹점 자동 카테고리 분류: 기존 매핑 우선, 없으면 AI 분류
        Integer spendingCategoryId = classifyMerchantCategory(merchantName);

        // 통합 금융 원장(financial_transaction_tbl) 생성
        org.scoula.cardpayment.dto.CardTransactionApproveDTO insertParam = org.scoula.cardpayment.dto.CardTransactionApproveDTO.builder()
                .userId(targetUserId)
                .receiveId(receiveId)
                .merchantName(merchantName)
                .amount(amount)
                .spendingCategoryId(spendingCategoryId)
                .build();

        cardPaymentMapper.insertFinancialTransactionForCard(insertParam);
        Integer createdTxId = insertParam.getTransactionId();

        // card_transaction_detail_tbl 상태 SUCCESS 및 transaction_id 업데이트
        cardPaymentMapper.updateCardTransactionStatus(cardTxId, "SUCCESS", createdTxId);
        Long currentWalletBalance = cardPaymentMapper.getWalletBalanceByUserId(targetUserId);

        log.info("카드 결제 승인 성공 (지갑 잔액 차감 없음) - CardTxID: {}, FinancialTxID: {}, UserID: {}, Merchant: {}, Amount: {}",
                cardTxId, createdTxId, targetUserId, merchantName, amount);

        //박우진 축하 , 결제 푸쉬 대신 이거 사용하실려면 사용하세요
//        taskEventService.sendTaskEvent(
//                targetUserId,
//                Enum.TaskType.PAYMENT_COMPLETE,
//                merchantName + "\n" + String.format("%,d원 결제완료.", amount),
//                null
//        );
        return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                .cardTransactionId(cardTxId)
                .linkedCardId(detailVO.getLinkedCardId())
                .status("SUCCESS")
                .transactionId(createdTxId)
                .merchantName(merchantName)
                .amount(amount)
                .updatedWalletBalance(currentWalletBalance)
                .message("카드 결제가 성공적으로 승인되었습니다.")
                .build();
    }

    // 2. 전자지갑 / QR / 바코드 결제 승인 (wallet_tbl 잔액 직접 차감)
    @Override
    @Transactional
    public org.scoula.cardpayment.dto.CardTransactionResponseDTO approveWalletTransaction(org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO) {
        log.info("전자지갑/QR/바코드 결제 승인 요청 (wallet_tbl 잔액 차감) - DTO: {}", approveDTO);

        Integer targetUserId = approveDTO.getUserId();
        if (targetUserId == null || targetUserId <= 0) {
            targetUserId = 1;
        }

        Long cardTxId = approveDTO.getCardTransactionId();
        org.scoula.cardpayment.domain.CardTransactionDetailVO detailVO = null;

        if (cardTxId != null && cardTxId > 0) {
            detailVO = cardPaymentMapper.getCardTransactionDetailById(cardTxId);
        } else {
            detailVO = cardPaymentMapper.getPendingTransactionByUserId(targetUserId);
        }

        if (detailVO == null || !"PENDING".equalsIgnoreCase(detailVO.getStatus())) {
            log.warn("전자지갑/QR/바코드 결제 승인 실패: 유효한 결제 대기(PENDING) 건이 없습니다. UserID: {}", targetUserId);
            return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                    .status("FAILED")
                    .message("현재 활성화된 QR/바코드 결제 대기 상태가 아닙니다. 지갑 결제(QR/바코드) 화면을 열고 다시 시도해 주세요.")
                    .build();
        }

        cardTxId = detailVO.getCardTransactionId();

        String merchantName = (approveDTO.getMerchantName() != null && !approveDTO.getMerchantName().isBlank())
                ? approveDTO.getMerchantName().trim() : "스타벅스";
        Integer amount = (approveDTO.getAmount() != null && approveDTO.getAmount() > 0)
                ? approveDTO.getAmount() : 10000;

        // 전자지갑(wallet_tbl) 잔액 차감시도
        int walletSubtracted = cardPaymentMapper.subtractWalletBalance(targetUserId, amount);
        if (walletSubtracted == 0) {
            log.warn("전자지갑(wallet_tbl) 잔액 부족으로 결제 승인 실패 - UserID: {}, Amount: {}", targetUserId, amount);
            cardPaymentMapper.updateCardTransactionStatus(cardTxId, "FAILED", null);
            Long currentWalletBal = cardPaymentMapper.getWalletBalanceByUserId(targetUserId);

            return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                    .cardTransactionId(cardTxId)
                    .linkedCardId(detailVO.getLinkedCardId())
                    .status("FAILED")
                    .merchantName(merchantName)
                    .amount(amount)
                    .updatedWalletBalance(currentWalletBal)
                    .message("전자지갑(Wallet) 잔액이 부족합니다.")
                    .build();
        }

        Long updatedWalletBalance = cardPaymentMapper.getWalletBalanceByUserId(targetUserId);

        Integer merchantAccountId = approveDTO.getMerchantAccountId();
        if (merchantAccountId == null) {
            merchantAccountId = cardPaymentMapper.getMerchantAccountIdByName(merchantName);
            if (merchantAccountId == null) merchantAccountId = 99;
        }
        Integer receiveId = cardPaymentMapper.getMerchantUserIdByAccountId(merchantAccountId);

        // 박준우: 결제 가맹점 자동 카테고리 분류: 기존 매핑 우선, 없으면 AI 분류
        Integer spendingCategoryId = classifyMerchantCategory(merchantName);

        org.scoula.cardpayment.dto.CardTransactionApproveDTO insertParam = org.scoula.cardpayment.dto.CardTransactionApproveDTO.builder()
                .userId(targetUserId)
                .receiveId(receiveId)
                .merchantName(merchantName)
                .amount(amount)
                .spendingCategoryId(spendingCategoryId)
                .build();

        cardPaymentMapper.insertFinancialTransactionForCard(insertParam);
        Integer createdTxId = insertParam.getTransactionId();

        cardPaymentMapper.updateCardTransactionStatus(cardTxId, "SUCCESS", createdTxId);

        // 지갑/QR/바코드 결제 승인 알림 발송 (NotificationRequestDTO 규격 연동)
        try {
            Integer safeTxId = (createdTxId != null && createdTxId > 0) ? createdTxId : 1;
            notificationService.create(
                    org.scoula.notification.dto.NotificationRequestDTO.builder()
                            .receiverId(targetUserId)
                            .senderId(targetUserId)
                            .notificationType(org.scoula.common.util.Enum.NotificationType.SETTLEMENT_PAYMENT)
                            .targetId(safeTxId)
                            .build()
            );
        } catch (Throwable notifErr) {
            log.warn("결제 승인 알림 전송 중 예외 (결제 승인은 정상 완료): {}", notifErr.getMessage());
        }

        log.info("전자지갑/QR/바코드 결제 승인 성공 - CardTxID: {}, FinancialTxID: {}, UserID: {}, Merchant: {}, Amount: {}, UpdatedWalletBalance: {}",
                cardTxId, createdTxId, targetUserId, merchantName, amount, updatedWalletBalance);

        return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                .cardTransactionId(cardTxId)
                .linkedCardId(detailVO.getLinkedCardId())
                .status("SUCCESS")
                .transactionId(createdTxId)
                .merchantName(merchantName)
                .amount(amount)
                .updatedWalletBalance(updatedWalletBalance)
                .message("전자지갑 결제가 성공적으로 승인되었습니다.")
                .build();
    }

    /**
     * 박준우: 결제 가맹점의 소비 카테고리를 자동 결정한다.
     * 기존 가맹점 매핑을 먼저 사용하고, 매핑(임시테이블 저장)이 없을 때만 AI를 호출한다.
     * AI 분류가 실패하면 null을 반환하여 미분류 상태로 거래는 정상 저장한다.
     */
    private Integer classifyMerchantCategory(String merchantName) {
        if (merchantName == null || merchantName.isBlank()) {
            return null;
        }

        MerchantCategoryClassificationResultDTO result =
                merchantCategoryService.classify(merchantName.trim());

        if (result == null || result.getSpendingCategoryId() == null) {
            log.warn("결제 가맹점 카테고리 자동 분류 실패 - merchantName={}", merchantName);
            return null;
        }

        log.info(
                "결제 가맹점 카테고리 자동 분류 완료 - merchantName={}, categoryId={}, source={}",
                merchantName,
                result.getSpendingCategoryId(),
                result.getClassificationSource()
        );

        return result.getSpendingCategoryId();
    }

    @Override
    public org.scoula.cardpayment.dto.CardTransactionResponseDTO getTransactionStatus(Long cardTransactionId) {
        org.scoula.cardpayment.domain.CardTransactionDetailVO detailVO = cardPaymentMapper.getCardTransactionDetailById(cardTransactionId);
        if (detailVO == null) {
            return null;
        }

        return org.scoula.cardpayment.dto.CardTransactionResponseDTO.builder()
                .cardTransactionId(detailVO.getCardTransactionId())
                .linkedCardId(detailVO.getLinkedCardId())
                .status(detailVO.getStatus())
                .createdAt(detailVO.getCreatedAt())
                .transactionId(detailVO.getTransactionId())
                .amount(detailVO.getAmount())
                .merchantName(detailVO.getMerchantName())
                .build();
    }

    @Override
    @Transactional
    public boolean cancelTransaction(Long cardTransactionId) {
        if (cardTransactionId == null || cardTransactionId <= 0) {
            return false;
        }

        org.scoula.cardpayment.domain.CardTransactionDetailVO detailVO = cardPaymentMapper.getCardTransactionDetailById(cardTransactionId);
        if (detailVO != null && "PENDING".equalsIgnoreCase(detailVO.getStatus())) {
            log.info("결제 대기 건 취소/만료 FAILED 처리 - CardTxID: {}", cardTransactionId);
            cardPaymentMapper.updateCardTransactionStatus(cardTransactionId, "FAILED", null);
            return true;
        }

        return false;
    }
}
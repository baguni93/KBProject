package org.scoula.cardpayment.service;

import org.scoula.cardpayment.dto.CardAgreementDTO;
import org.scoula.cardpayment.dto.CardBinResponseDTO;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.CardStatusResponseDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;

public interface CardPaymentService {

    PrimaryCardResponseDTO getPrimaryCard(Integer userId);

    CardStatusResponseDTO getCardStatus(Integer userId);

    PrimaryCardResponseDTO registerCard(CardRegisterDTO cardRegisterDTO);

    boolean saveCardAgreements(CardAgreementDTO cardAgreementDTO);

    boolean setPrimaryCard(Integer cardId, Integer userId);

    CardBinResponseDTO getAutoFetchedCardInfo(String binNumber);

    // 1단계: 결제 대기(PENDING) 생성
    org.scoula.cardpayment.dto.CardTransactionResponseDTO createPendingTransaction(org.scoula.cardpayment.dto.CardTransactionRequestDTO requestDTO);

    // 카드 결제 승인 요청 (지갑 차감 없이 결제 기록만 생성)
    org.scoula.cardpayment.dto.CardTransactionResponseDTO approveTransaction(org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO);

    // 전자지갑 / QR / 바코드 결제 승인 요청 (wallet_tbl 잔액 차감)
    org.scoula.cardpayment.dto.CardTransactionResponseDTO approveWalletTransaction(org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO);

    // 결제 상태 조회
    org.scoula.cardpayment.dto.CardTransactionResponseDTO getTransactionStatus(Long cardTransactionId);

    // 결제 취소/만료 시 FAILED 처리
    boolean cancelTransaction(Long cardTransactionId);
}

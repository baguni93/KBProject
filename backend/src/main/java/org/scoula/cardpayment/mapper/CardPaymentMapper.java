package org.scoula.cardpayment.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.cardpayment.dto.CardRegisterDTO;
import org.scoula.cardpayment.dto.PrimaryCardResponseDTO;
import org.scoula.wallet.dto.RegisteredCardDTO;

import java.util.List;

@Mapper
public interface CardPaymentMapper {

    PrimaryCardResponseDTO getPrimaryCardByUserId(@Param("userId") Integer userId);

    List<RegisteredCardDTO> getCardsByUserId(@Param("userId") Integer userId);

    int countCardsByUserId(@Param("userId") Integer userId);

    int countPrimaryCardsByUserId(@Param("userId") Integer userId);

    int insertCard(CardRegisterDTO cardRegisterDTO);

    int insertLinkedCard(CardRegisterDTO cardRegisterDTO);

    int resetPrimaryCardByUserId(@Param("userId") Integer userId);

    int resetLinkedPrimaryCardByUserId(@Param("userId") Integer userId);

    int setPrimaryCard(@Param("cardId") Integer cardId, @Param("userId") Integer userId);

    int insertUserAgreement(@Param("userId") Integer userId, @Param("agreementId") Integer agreementId, @Param("agreedYn") String agreedYn);

    Integer validateCard(CardRegisterDTO cardRegisterDTO);

    int insertOrUpdateCardProduct(@Param("cardName") String cardName, @Param("cardType") String cardType, @Param("cardImage") String cardImage, @Param("annualFee") int annualFee);

    // 카드 결제 거래 상세 (card_transaction_detail_tbl) 관련
    int insertCardTransactionDetail(org.scoula.cardpayment.domain.CardTransactionDetailVO vo);

    org.scoula.cardpayment.domain.CardTransactionDetailVO getCardTransactionDetailById(@Param("cardTransactionId") Long cardTransactionId);

    org.scoula.cardpayment.domain.CardTransactionDetailVO getLatestPendingTransaction();

    org.scoula.cardpayment.domain.CardTransactionDetailVO getPendingTransactionByUserId(@Param("userId") Integer userId);

    int updateCardTransactionStatus(@Param("cardTransactionId") Long cardTransactionId, @Param("status") String status, @Param("transactionId") Integer transactionId);

    Integer getUserIdByLinkedCardId(@Param("linkedCardId") Integer linkedCardId);

    Integer getUserAccountId(@Param("userId") Integer userId);

    Integer getMerchantAccountIdByName(@Param("merchantName") String merchantName);

    Integer getMerchantUserIdByAccountId(@Param("accountId") Integer accountId);

    int subtractAccountBalance(@Param("accountId") Integer accountId, @Param("amount") Integer amount);

    int addAccountBalanceById(@Param("accountId") Integer accountId, @Param("amount") Integer amount);

    int insertFinancialTransactionForCard(org.scoula.cardpayment.dto.CardTransactionApproveDTO approveDTO);
}

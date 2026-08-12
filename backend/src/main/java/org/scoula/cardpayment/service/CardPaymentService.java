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
}

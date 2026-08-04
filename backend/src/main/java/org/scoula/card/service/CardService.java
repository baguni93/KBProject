package org.scoula.card.service;

import org.scoula.card.dto.CardAgreementDTO;
import org.scoula.card.dto.CardRegisterDTO;
import org.scoula.card.dto.CardStatusResponseDTO;
import org.scoula.card.dto.PrimaryCardResponseDTO;

public interface CardService {

    PrimaryCardResponseDTO getPrimaryCard(Integer userId);

    CardStatusResponseDTO getCardStatus(Integer userId);

    PrimaryCardResponseDTO registerCard(CardRegisterDTO cardRegisterDTO);

    boolean saveCardAgreements(CardAgreementDTO cardAgreementDTO);

    boolean setPrimaryCard(Integer cardId, Integer userId);
}

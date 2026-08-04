package org.scoula.card.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.scoula.card.dto.CardRegisterDTO;
import org.scoula.card.dto.PrimaryCardResponseDTO;
import org.scoula.wallet.dto.RegisteredCardDTO;

import java.util.List;

@Mapper
public interface CardMapper {

    PrimaryCardResponseDTO getPrimaryCardByUserId(@Param("userId") Integer userId);

    List<RegisteredCardDTO> getCardsByUserId(@Param("userId") Integer userId);

    int countCardsByUserId(@Param("userId") Integer userId);

    int countPrimaryCardsByUserId(@Param("userId") Integer userId);

    int insertCard(CardRegisterDTO cardRegisterDTO);

    int resetPrimaryCardByUserId(@Param("userId") Integer userId);

    int setPrimaryCard(@Param("cardId") Integer cardId, @Param("userId") Integer userId);

    int insertUserAgreement(@Param("userId") Integer userId, @Param("agreementId") Integer agreementId, @Param("agreedYn") String agreedYn);
}

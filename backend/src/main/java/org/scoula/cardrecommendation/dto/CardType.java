package org.scoula.cardrecommendation.dto;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;

import java.util.Locale;

public enum CardType {
    CREDIT,
    CHECK;

    public static CardType from(String value) {
        if (value == null || value.isBlank()) {
            return CREDIT;
        }

        try {
            return CardType.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_CARD_RECOMMENDATION_OPTION);
        }
    }
}

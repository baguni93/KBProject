package org.scoula.cardrecommendation.dto;

import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;

import java.util.Locale;

public enum FeeMode {
    MAX_BENEFIT,
    NET_BENEFIT;

    public static FeeMode from(String value) {
        if (value == null || value.isBlank()) {
            return MAX_BENEFIT;
        }

        try {
            return FeeMode.valueOf(value.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_CARD_RECOMMENDATION_OPTION);
        }
    }
}

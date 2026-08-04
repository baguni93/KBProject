package org.scoula.card.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardAgreementDTO {
    private Integer userId;
    private List<Integer> agreementIds;
    private String agreedYn;
}

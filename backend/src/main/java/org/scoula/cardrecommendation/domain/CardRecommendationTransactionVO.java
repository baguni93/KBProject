package org.scoula.cardrecommendation.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CardRecommendationTransactionVO {
    private Integer transactionId;
    private Integer amount;
    private LocalDateTime createdAt;
    private Integer spendingCategoryId;
    private Integer parentCategoryId;
}

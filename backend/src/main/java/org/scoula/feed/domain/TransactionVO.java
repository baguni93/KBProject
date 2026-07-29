package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionVO {

    private int transactionId;
    private int receiveId;
    private int spendingCategoryId;
    private String transactionType;
    private ProfileSimpleVO receiver;
    private CategoryVO category;
}

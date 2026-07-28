package org.scoula.settlement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.CategoryVO;
import org.scoula.feed.domain.ProfileSimpleVO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementVO {

    private int settlementId; //pk

    private int requesterId;
    private String title;
    private String content;
    private int totalAmount;
    private Enum.SettlementStatus status;
    private int spendingCategoryId;

    private Date createdAt;
    private Date completedAt;
    private Date lastReminderDate;

    private Enum.SettlementType settlementType;

    private List<SettlementMemberVO> members;

    private ProfileSimpleVO sender;
    private CategoryVO category;

}

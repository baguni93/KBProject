package org.scoula.settlement.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.settlement.dto.SettlementMemberRequestDTO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementMemberVO {

    private int settlementMemberId; //pk
    private int settlementId; //fk
    private int userId; //fk
    private int amount;
    private Date completedAt;
    private Enum.SettlementStatus status; //정산 상태
    private ProfileSimpleVO receiver;

    public static SettlementMemberVO of(int settlementId , SettlementMemberRequestDTO settlementMemberRequestDTO){
        return settlementMemberRequestDTO == null ?  null :
                SettlementMemberVO.builder()
                        .settlementId(settlementId)
                        .userId(settlementMemberRequestDTO.getUserId())
                        .amount(settlementMemberRequestDTO.getAmount())
                        .build();
    }
}

package org.scoula.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.settlement.domain.SettlementMemberVO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SettlementMemberResponseDTO {

    private int userId;
    private Enum.SettlementStatus status; //송금 상황
    private int amount;
    private Date completedAt;

    private ProfileSimpleVO receiver;

    public static  SettlementMemberResponseDTO of(SettlementMemberVO settlementMemberVO){
        return settlementMemberVO == null? null
                : SettlementMemberResponseDTO
                .builder()
                .userId(settlementMemberVO.getUserId())
                .status(settlementMemberVO.getStatus())
                .amount(settlementMemberVO.getAmount())
                .completedAt(settlementMemberVO.getCompletedAt())
                .receiver(settlementMemberVO.getReceiver())
                .build();
    }

}

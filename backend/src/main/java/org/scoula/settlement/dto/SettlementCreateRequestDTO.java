package org.scoula.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.settlement.domain.SettlementVO;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementCreateRequestDTO {

    private int requesterId;
    private String title;
    private String content;
    private int totalAmount;
    private int spendingCategoryId;
    private Enum.SettlementType settlementType;

    private List<SettlementMemberRequestDTO> members;

    public SettlementVO toVo(){
        return SettlementVO.builder()
                .requesterId(requesterId)
                .title(title)
                .content(content)
                .totalAmount(totalAmount)
                .spendingCategoryId(spendingCategoryId)
                .settlementType(settlementType)
                .build();
    }


    //이미지 첨부파일 어떻게 할까...
}

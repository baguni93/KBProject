package org.scoula.settlement.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.CategoryVO;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.settlement.domain.SettlementVO;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SettlementResponseDTO {

    private int settlementId; // 정산 id
    private int requesterId; // 요청자 id
    private String title; // 제목
    private String content; // 내용
    private int totalAmount; // 총 금액
    private Date createdAt;
    private Date completedAt;
    private Date lastReminderDate;
    private Enum.SettlementStatus status; //현재 상황

    private ProfileSimpleVO profileSimpleVO;
    private CategoryVO categoryVO;

    private List<SettlementMemberResponseDTO> members;

    public static SettlementResponseDTO of(SettlementVO settlementVO) {

       return  settlementVO == null ? null :  SettlementResponseDTO.builder()
                .settlementId(settlementVO.getSettlementId())
                .requesterId(settlementVO.getRequesterId())
                .title(settlementVO.getTitle())
                .content(settlementVO.getContent())
                .totalAmount(settlementVO.getTotalAmount())
                .createdAt(settlementVO.getCreatedAt())
                .completedAt(settlementVO.getCompletedAt())
                .lastReminderDate(settlementVO.getLastReminderDate())
                .status(settlementVO.getStatus())
                .members(settlementVO.getMembers().stream().map(SettlementMemberResponseDTO::of).toList())
                .profileSimpleVO(settlementVO.getSender())
                .categoryVO(settlementVO.getCategory())
                .build();
    }
}

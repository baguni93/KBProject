package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomBoxIssueResultDTO {

    private Integer userRandomBoxId;
    private String issueReason;
    private String boxStatus;
    private String issuedAt;

    private boolean issued; // true면 랜덤박스가 실제 지급됨, false면 1000원 미만이라 지급X
    private String message; //
}
package org.scoula.customcard.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.customcard.domain.CheckCanIssueVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckCanIssueDTO {

    private int userId;
    private String accountNumber;
    private Enum.CheckCanIssueStatus checkCanIssueStatus;
    private String verificationCode;
    private Long verificationId;

    public static CheckCanIssueDTO of(CheckCanIssueVO checkCanIssueVO , Long verificationId , String verificationCode){
        return checkCanIssueVO == null ? null : CheckCanIssueDTO.builder()
                .checkCanIssueStatus(checkCanIssueVO.getCheckCanIssueStatus())
                .verificationCode(verificationCode)
                .verificationId(verificationId)
                .build();

    }
}

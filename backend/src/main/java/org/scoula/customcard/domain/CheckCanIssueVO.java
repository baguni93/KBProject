package org.scoula.customcard.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CheckCanIssueVO {

    private int userId;
    private String bankCode;
    private String userName;
    private Enum.CheckCanIssueStatus checkCanIssueStatus;
}

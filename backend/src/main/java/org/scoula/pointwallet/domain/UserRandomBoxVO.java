package org.scoula.pointwallet.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.pointwallet.dto.RandomBoxIssueResultDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenResultDTO;
import org.scoula.pointwallet.dto.UserRandomBoxDTO;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRandomBoxVO {

    private Integer userRandomBoxId;
    private Integer userId;

    private String issueReason;

    // 출석 ID, 피드 ID, 송금 거래 ID, 이벤트 참여 ID
    private Integer sourceId;

    // TRANSFER일 때만 값 존재
    private Integer targetAccountId;

    private String boxStatus;
    private Integer rewardPoint;

    private String issuedAt;
    private String openedAt;

    // 랜덤박스 발급 결과를 DTO로 변환
    public RandomBoxIssueResultDTO toIssueResultDTO() {

        return RandomBoxIssueResultDTO.builder()
                .issued(true) // 랜덤박스 지급되었음을 표기한다.
                .message("랜덤박스가 지급되었습니다.")
                .userRandomBoxId(userRandomBoxId)
                .issueReason(issueReason)
                .boxStatus(boxStatus)
                .issuedAt(issuedAt)
                .build();
}
    // 개봉된 랜덤박스를 개봉 결과 DTO로 변환
    public RandomBoxOpenResultDTO toOpenResultDTO(
            Integer pointBalance
    ) {
        return RandomBoxOpenResultDTO.builder()
                .userRandomBoxId(userRandomBoxId)
                .rewardPoint(rewardPoint)
                .boxStatus(boxStatus)
                .openedAt(openedAt)
                .pointBalance(pointBalance)
                .build();
    }

    // 미개봉 목록이나, 일반 랜덤박스 조회
    public UserRandomBoxDTO toDTO() {

        return UserRandomBoxDTO.builder()
                .userRandomBoxId(userRandomBoxId)
                .userId(userId)
                .issueReason(issueReason)
                .sourceId(sourceId)
                .boxStatus(boxStatus)
                .rewardPoint(rewardPoint)
                .issuedAt(issuedAt)
                .openedAt(openedAt)
                .build();
    }
}
package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserRandomBoxDTO {

    private Integer userRandomBoxId;
    private Integer userId;

    // 랜덤박스 발급 사유: PAYMENT, ATTENDANCE, FEED_SHARE, FEED_RECEIVE_CLAIM, EVENT
    private String issueReason;

    // 발급 원인이 된 출석·피드·이벤트 참여 내역의 PK
    private Integer sourceId;

    private String boxStatus;
    private Integer rewardPoint;

    // 날짜 직렬화 문제를 피하기 위해 String 사용
    private String issuedAt;
    private String openedAt;
}
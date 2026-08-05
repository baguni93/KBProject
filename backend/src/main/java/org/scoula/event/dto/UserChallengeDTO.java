package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserChallengeDTO {
    // 사용자 이벤트 챌린지 현황
    private Integer userChallengeLevel;     // 현재 챌린지 레벨
    private Integer userChallengeExe;       // 현재 누적 경험치
    private Integer userChallengeMaxExe;    // 레벨업에 필요한 목표 경험치
    private String status;                  // 보상 수령 상태(PROCESS, COMPLETE, REWARDED)


    private String challengeStartAt; // 챌린지 시작일자
    private String challengeEndAt;   // 챌린지 종료일자
    private String challengeDDay;           // 챌린지 남은 기간 (D-day)
}

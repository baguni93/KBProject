package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMainDTO { // 이벤트 메인화면
    private Integer userId;          // 사용자id

    // 포인트 조회
    private Integer currentPoint;    // 현재 보유 포인트

    // 이벤트 챌린지
    private Integer userChallengeLevel;     // 현재 챌린지 레벨
    private Integer userChallengeExe;       // 현재 누적 경험치
    private Integer userChallengeMaxExe;    // 레벨업에 필요한 목표 경험치

    private String challengeStartAt; // 챌린지 시작일자
    private String challengeEndAt;   // 챌린지 종료일자
    private String challengeDDay;    // 챌린지 남은 기간(D-day)

    // 이벤트 리스트
    private List<EventGetResponseDTO> eventLists;
    // 사용자 이벤트 챌린지 현황
    private UserChallengeDTO userChallenge;


}

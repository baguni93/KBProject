package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.event.domain.EventChallengeVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChallengeDTO {
    private int challengeId;
    private String challengeName;
    private Integer rewardPoint;
    private Integer maxLevel;     // 챌린지 레벨
    private Integer maxTarget;
    private Date startDate; // 챌린지 시작일자
    private Date endDate;   // 챌린지 종료일자
   // private String challengeDDay;           // 챌린지 남은 기간 (D-day)

    public static EventChallengeDTO of(EventChallengeVO eventChallengeVO) {
        return eventChallengeVO == null ? null : EventChallengeDTO.builder()
                .challengeId(eventChallengeVO.getChallengeId())
                .challengeName(eventChallengeVO.getChallengeName())
                .rewardPoint(eventChallengeVO.getRewardPoint())
                .maxLevel(eventChallengeVO.getMaxLevel())
                .maxTarget(eventChallengeVO.getMaxTarget())
                .startDate(eventChallengeVO.getStartDate())
                .endDate(eventChallengeVO.getEndDate())
                //.challengeDDay(eventChallengeVO.getChallengeDDay())
                .build();
    }
}

package org.scoula.event.dto;

import io.swagger.models.auth.In;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.event.domain.EventChallengeUserVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChallengeResponseDTO {
    private Integer challengeId;
    private Integer userChallengeId;
    private Integer userId;
    private Integer currentLevel;     // 현재 챌린지 레벨
    private Integer currentTarget;    // 레벨업에 필요한 목표 경험치
    private String status;            // 보상 수령 상태(PROCESS, COMPLETE, REWARDED)
    private Integer exp;
    private Date updatedAt;
    private Integer challengeLevelId;
    private Integer level;
    private Integer requiredExp;
    private Integer rewardPoint;

    public static EventChallengeResponseDTO of(EventChallengeUserVO eventChallengeUserVO) {
        return eventChallengeUserVO == null ? null : EventChallengeResponseDTO.builder()
                .challengeId(eventChallengeUserVO.getChallengeId())
                .userChallengeId(eventChallengeUserVO.getUserChallengeId())
                .currentLevel(eventChallengeUserVO.getCurrentLevel())
                .currentTarget(eventChallengeUserVO.getCurrentTarget())
                .status(eventChallengeUserVO.getStatus())
                .exp(eventChallengeUserVO.getExp())
                .updatedAt(eventChallengeUserVO.getUpdatedAt())
                .challengeLevelId(eventChallengeUserVO.getChallengeLevelId())
                .level(eventChallengeUserVO.getLevel())
                .requiredExp(eventChallengeUserVO.getRequiredExp())
                .rewardPoint(eventChallengeUserVO.getRewardPoint())
                .build();
    }
}

package org.scoula.event.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventChallengeUserVO {
    private Integer userChallengeId;
    private Integer userId;
    private Integer challengeId;
    private Integer challengeLevel;
    private Integer challengeExe;
    private String status;
    private LocalDateTime updatedAt;
}

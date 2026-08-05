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
public class EventParticipationVO {
    private Integer participationId;
    private Integer eventId;
    private Integer userId;
    private LocalDateTime participatedAt;
}

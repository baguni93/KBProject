package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventMainResponseDTO {
    private Integer userId;
    private int currentPoint;

    private UserChallengeDTO userChallenge;

    private List<EventResponseDTO> activeEvents;
}

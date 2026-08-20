package org.scoula.event.dto;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventCompletionDTO {

    private Integer eventId;
    private String eventName;
    private Boolean completed;
}
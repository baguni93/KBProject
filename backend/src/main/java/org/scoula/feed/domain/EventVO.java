package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EventVO {

    private int receiveId;
    private int eventId;
    private int rewardId;
    private String eventImageName;
    private int rewardPoint;
}

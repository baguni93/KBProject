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
    private String eventName;
    private int rewardPoint;

    // 프론트엔드에서 사용할 url 프로퍼티
    public String getUrl() {
        return "/api/feeds/eventImage/" + eventImageName;
    }
}

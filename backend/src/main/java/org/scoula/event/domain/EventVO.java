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
public class EventVO {
    private Integer eventId;    //이벤트id
    private String eventName;   //이벤트명
    private String eventDesc;   //이벤트설명
    private String eventType;   //이벤트종류
    private String eventStatus; //이벤트상태(OPEN/CLOSE)
    private String eventImgName;    //이미지로고파일명
    private Integer eventTarget;    //이벤트최종목표
    private Integer eventLevel;     //이벤트최총난이도
    private Integer eventDailyLimitCount;   // 이벤트 일일 참여가능 횟수
    private LocalDateTime startAt;  //시작일시
    private LocalDateTime endAt;    //종료일시
    private LocalDateTime createdAt;    //등록일시
}

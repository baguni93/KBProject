package org.scoula.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventResponseDTO {
    // 이벤트
    private Integer eventId;
    private String eventName;
    private String eventDesc;
    private String eventType;
    private String eventCategory;
    private String eventImgName;
    private Integer eventDailyLimitCount;
    private Integer eventTarget;
    // 이벤트 리워드
    private Integer rewardId;
    private Integer rewardPoint;
    private Integer rewardExp;
    private Integer reqCount;
    private String useYn;
    private Integer currentLevel;
    private Integer currentTarget;
    private Integer partCount;
    // 버튼 관련 텍스트 제어는 프론트에서 처리할 예정
    // 우측 배지 버튼
    // ex)출석체크 → 출석 / 출석완료
    // 참여 / 참여완료
    // 보상받기 / 수령완료
    private String buttonStatus;
    private String endAt;    // 실제 이벤트 종료 시간
    private LocalDateTime  receivedAt;
    private String dDay;    // 매일 출석체크 -> 상시, 그 이외는 D-day 형식

}

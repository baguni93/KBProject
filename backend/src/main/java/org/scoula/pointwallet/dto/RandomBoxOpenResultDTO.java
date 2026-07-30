package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomBoxOpenResultDTO {


    private Integer userRandomBoxId; // 개봉한 랜덤박스 ID
    private Integer rewardPoint; // 해당 박스에서 나온 포인트
    private String boxStatus; // OPENED
    private String openedAt; // 개봉일시
    private Integer pointBalance; // 포인트 적립 후 최종 잔액

    public RandomBoxOpenResultDTO toOpenResultDTO(
            Integer pointBalance
    ) {
        return RandomBoxOpenResultDTO.builder()
                .userRandomBoxId(userRandomBoxId)
                .rewardPoint(rewardPoint)
                .boxStatus(boxStatus)
                .openedAt(openedAt)
                .pointBalance(pointBalance)
                .build();
    }
}
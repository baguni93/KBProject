package org.scoula.pointwallet.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RandomBoxOpenAllResultDTO {

    private Integer openedCount; // 개봉한 요청한 박스 수
    private Integer totalRewardPoint; // 모든 박스의 포인트 합계
    private Integer pointBalance; // 최종 포인트 잔액
    private List<RandomBoxOpenResultDTO> openedBoxes; // 박스별 개봉 결과


}
package org.scoula.pointwallet.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.pointwallet.domain.UserRandomBoxVO;
import org.scoula.pointwallet.dto.UserRandomBoxDTO;

import java.util.List;

public interface RandomBoxMapper {

    // 동일한 원인으로 랜덤박스가 이미 발급됐는지 확인
    int countIssuedRandomBox(
            @Param("userId") Integer userId,
            @Param("issueReason") String issueReason,
            @Param("sourceId") Integer sourceId
    );

    // 미개봉 랜덤박스 생성
    int insertRandomBox(
            UserRandomBoxVO randomBox
    );

    // 생성된 랜덤박스 조회
    UserRandomBoxVO selectRandomBoxById(
            @Param("userRandomBoxId") Integer userRandomBoxId,
            @Param("userId") Integer userId
    );

    // 사용자가 발급사유별 랜덤박스를 몇개 받았는지 조회
    int countTodayIssuedByReason(
            @Param("userId") Integer userId,
            @Param("issueReason") String issueReason
    );

    // 결제 ID나 피드ID로 랜덤박스를 찾기.
    UserRandomBoxVO selectRandomBoxBySource(
            @Param("userId") Integer userId,
            @Param("issueReason") String issueReason,
            @Param("sourceId") Integer sourceId
    );

    // 랜덤박스 발급을 취소하는 요청.
    int revokeRandomBox(
            @Param("userRandomBoxId") Integer userRandomBoxId,
            @Param("userId") Integer userId,
            @Param("expectedStatus") String expectedStatus,
            @Param("revokeReason") String revokeReason
    );

    // 랜덤박스 개봉 전 잠금 조회
    UserRandomBoxVO selectRandomBoxForUpdate(
            @Param("userRandomBoxId") Integer userRandomBoxId,
            @Param("userId") Integer userId
    );

    // 미개봉 랜덤박스 개봉 처리
    int openRandomBox(
            @Param("userRandomBoxId") Integer userRandomBoxId,
            @Param("userId") Integer userId,
            @Param("rewardPoint") Integer rewardPoint
    );

    // 사용자의 미개봉 랜덤박스 개수 조회
    int countUnopenedRandomBoxes(
            @Param("userId") Integer userId
    );

    // 사용자의 미개봉 랜덤박스 목록 조회
    List<UserRandomBoxVO> selectUnopenedRandomBoxes(
            @Param("userId") Integer userId
    );

    // 모두 열기 전 미개봉 랜덤박스 row Lock 조회
    List<UserRandomBoxVO> selectUnopenedRandomBoxesForUpdate(
            @Param("userId") Integer userId
    );
}
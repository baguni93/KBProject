package org.scoula.pointwallet.service;

import org.scoula.pointwallet.dto.RandomBoxIssueResultDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenAllResultDTO;
import org.scoula.pointwallet.dto.RandomBoxOpenResultDTO;
import org.scoula.pointwallet.dto.UserRandomBoxDTO;

import java.util.List;

public interface RandomBoxService {
    // 출석 완료 보상으로 랜덤박스 1개 지급
    RandomBoxIssueResultDTO issueForAttendance(
            Integer userId,
            Integer attendanceId
    );


    // 피드(친구/공개) 송신 완료 보상
    RandomBoxIssueResultDTO issueForFeedShare(
            Integer senderUserId,
            Integer feedId
    );


    // 이벤트 참여 완료 보상
    RandomBoxIssueResultDTO issueForEvent(
            Integer userId,
            Integer eventParticipationId
    );



    // 송금 했을 때 랜덤박스 제공
    RandomBoxIssueResultDTO issueForTransfer(
            Integer senderUserId, // 송금한 사용자
            Integer transactionId, // 실제 성공한 송금 거래 ID + 랜덤박스 발급 원인 추적용
            Integer targetAccountId // 수취계좌 ID, 계좌당 한번 지급 중복 검사 용도
    );


    // 랜덤박스 1개 개봉
    RandomBoxOpenResultDTO openRandomBox(
            Integer userId,
            Integer userRandomBoxId
    );

    // 미개봉 랜덤박스 개수 조회
    int getUnopenedRandomBoxCount(
            Integer userId
    );

    // 사용자의 미개봉 랜덤박스 목록 조회
    List<UserRandomBoxDTO> getUnopenedRandomBoxes(
            Integer userId
    );

    // 사용자의 미개봉 랜덤박스 모두 개봉
    RandomBoxOpenAllResultDTO openAllRandomBoxes(
            Integer userId
    );




}
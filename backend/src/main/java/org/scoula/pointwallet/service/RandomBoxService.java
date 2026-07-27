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

    // 랜덤박스 결제
    RandomBoxIssueResultDTO issueForPayment(
            Integer userId,
            Integer transactionId,
            Integer paymentAmount
    );


    // 피드 송신 완료 보상
    RandomBoxIssueResultDTO issueForFeedShare(
            Integer senderUserId,
            Integer feedId
    );

    // 피드 수신 및 받기 보상
    RandomBoxIssueResultDTO claimFromReceivedFeed(
            Integer receiverUserId,
            Integer feedId
    );

    // 이벤트 참여 완료 보상
    RandomBoxIssueResultDTO issueForEvent(
            Integer userId,
            Integer eventParticipationId
    );

    // 랜덤박스 취소
    void revokePaymentRandomBox(
            Integer userId,
            Integer transactionId
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
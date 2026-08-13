package org.scoula.pointwallet.common;

public enum RandomBoxIssueReason {

    // 결제 정책의 최소 금액 이상 결제한 경우
    PAYMENT,

    // 내가 피드 공유
    FEED_SHARE,

    // 출석 체크를 완료한 경우
    ATTENDANCE,

    // 이벤트 참여 조건을 완료한 경우
    EVENT,

    // 송금한 경우 수취 대상 기준 1회 지급
    TRANSFER
}

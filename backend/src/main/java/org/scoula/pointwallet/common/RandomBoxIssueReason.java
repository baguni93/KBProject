package org.scoula.pointwallet.common;

public enum RandomBoxIssueReason {

    // 내가 피드 공유
    FEED_SHARE,

    // 출석 체크를 완료한 경우 (임시 용도 구현 안할 수도 있음)
    ATTENDANCE,

    // 이벤트 참여 조건을 완료한 경우 (추후 확장 용도 )
    EVENT,

    // 송금한 경우 계좌당 1개만 ㅇㅅㅇ
    TRANSFER


}

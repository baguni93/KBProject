package org.scoula.pointwallet.common;

public enum PointReasonType {

    DEPOSIT,        // 입금(AI가 멋대로 만든것. 사용 X, 기능에 문제는 없어서 DB에 수정은 안함)
    ATTENDANCE,     // 출석
    RANDOM_BOX,     // 랜덤박스
    CONVERSION,     // 전환
    EVENT           // 이벤트
}
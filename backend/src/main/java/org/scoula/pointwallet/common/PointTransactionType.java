package org.scoula.pointwallet.common;

public enum PointTransactionType {

    EARN,       // 적립
    USE,        // 사용
    EXPIRE,     // 만료 (현재 안씀)
    CANCEL      // 취소 또는 복구 (이벤트 등에서 취소 시킬 떄)
}


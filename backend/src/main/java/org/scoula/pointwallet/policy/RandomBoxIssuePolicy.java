package org.scoula.pointwallet.policy;

public final class RandomBoxIssuePolicy {

    /*
     * 결제 랜덤박스 최소 결제 금액.
     * 지급 기준 금액을 변경하려면 이 값만 수정하면 된다.
     */
    public static final int PAYMENT_MINIMUM_AMOUNT = 1_000;

    // 피드 송신 랜덤박스 일일 최대 발급 개수
    public static final int FEED_SHARE_DAILY_LIMIT = 10;

    private RandomBoxIssuePolicy() {
    }
}

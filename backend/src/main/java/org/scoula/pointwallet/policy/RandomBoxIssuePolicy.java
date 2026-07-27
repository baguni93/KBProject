package org.scoula.pointwallet.policy;



public final class RandomBoxIssuePolicy {

    // TODO-POLICY: 피드 송신 랜덤박스 일일 최대 발급 개수
    public static final int FEED_SHARE_DAILY_LIMIT = 10;

    // TODO-POLICY: 피드 수신 랜덤박스 일일 최대 발급 개수
    public static final int FEED_RECEIVE_DAILY_LIMIT = 10;

    // TODO-POLICY: 결제 랜덤박스 최소 지급 금액
    public static final int PAYMENT_MINIMUM_AMOUNT = 1000;

    private RandomBoxIssuePolicy() {
    }
}
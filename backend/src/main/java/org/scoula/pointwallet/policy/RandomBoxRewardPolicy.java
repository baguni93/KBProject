package org.scoula.pointwallet.policy;

import java.util.concurrent.ThreadLocalRandom;

public final class RandomBoxRewardPolicy {

    /*
     * 전체 확률 단위: 10,000
     *
     * 90%   = 9,000
     * 9%    =   900
     * 0.9%  =    90
     * 0.09% =     9
     * 0.01% =     1
     *
     * 랜덤박스 확률
     * 1~10P:  90%
     * 11~100P : 9%
     * 101~1000P: 1%
     * 1001~5000P: 0.09%
     * 10000P : 0.01%
     *
     * -> 인당 랜덤박스 1개 기대값은 18.6원 
     */
    private static final int TOTAL_PROBABILITY_WEIGHT = 10_000;

    private static final int REWARD_1_TO_10_WEIGHT = 9_000;
    private static final int REWARD_11_TO_100_WEIGHT = 900;
    private static final int REWARD_101_TO_1000_WEIGHT = 90;
    private static final int REWARD_1001_TO_5000_WEIGHT = 9;
    private static final int REWARD_10000_WEIGHT = 1;

    private static final int REWARD_MIN_1 = 1;
    private static final int REWARD_MAX_1 = 10;

    private static final int REWARD_MIN_2 = 11;
    private static final int REWARD_MAX_2 = 100;

    private static final int REWARD_MIN_3 = 101;
    private static final int REWARD_MAX_3 = 1_000;

    private static final int REWARD_MIN_4 = 1_001;
    private static final int REWARD_MAX_4 = 5_000;

    private static final int REWARD_JACKPOT = 10_000;

    private RandomBoxRewardPolicy() {
    }

    public static int drawRewardPoint() {

        validateProbabilityWeight();

        int randomNumber =
                ThreadLocalRandom.current()
                        .nextInt(
                                1,
                                TOTAL_PROBABILITY_WEIGHT + 1
                        );

        int cumulativeWeight =
                REWARD_1_TO_10_WEIGHT;

        if (randomNumber <= cumulativeWeight) {
            return drawPointInRange(
                    REWARD_MIN_1,
                    REWARD_MAX_1
            );
        }

        cumulativeWeight +=
                REWARD_11_TO_100_WEIGHT;

        if (randomNumber <= cumulativeWeight) {
            return drawPointInRange(
                    REWARD_MIN_2,
                    REWARD_MAX_2
            );
        }

        cumulativeWeight +=
                REWARD_101_TO_1000_WEIGHT;

        if (randomNumber <= cumulativeWeight) {
            return drawPointInRange(
                    REWARD_MIN_3,
                    REWARD_MAX_3
            );
        }

        cumulativeWeight +=
                REWARD_1001_TO_5000_WEIGHT;

        if (randomNumber <= cumulativeWeight) {
            return drawPointInRange(
                    REWARD_MIN_4,
                    REWARD_MAX_4
            );
        }

        cumulativeWeight +=
                REWARD_10000_WEIGHT;

        if (randomNumber <= cumulativeWeight) {
            return REWARD_JACKPOT;
        }

        throw new IllegalStateException(
                "랜덤박스 보상 추첨에 실패했습니다."
        );
    }

    private static int drawPointInRange(
            int minimumPoint,
            int maximumPoint
    ) {
        return ThreadLocalRandom.current()
                .nextInt(
                        minimumPoint,
                        maximumPoint + 1
                );
    }

    private static void validateProbabilityWeight() {

        int totalWeight =
                REWARD_1_TO_10_WEIGHT
                        + REWARD_11_TO_100_WEIGHT
                        + REWARD_101_TO_1000_WEIGHT
                        + REWARD_1001_TO_5000_WEIGHT
                        + REWARD_10000_WEIGHT;

        if (totalWeight
                != TOTAL_PROBABILITY_WEIGHT) {

            throw new IllegalStateException(
                    "랜덤박스 보상 확률의 합계가 100%가 아닙니다."
            );
        }
    }
}
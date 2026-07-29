package org.scoula.common.util;

public class Enum {

    public enum FeedType {
        TRANSFER,
        PAYMENT,
        SETTLEMENT,
        CARD,
        ANALYSIS,
        EVENT
    }

    public enum TransactionType {
        CHARGE,
        PAYMENT,
        TRANSFER,
        SETTLEMENT
    }

    public enum SettlementStatus{
        REQUEST,
        COMPLETE,
        CANCEL
    }

    public enum SettlementType{
        EQUAL,
        UNEQUAL
    }

    public enum VisibilityType{
        PUBLIC,
        FRIEND,
        PRIVATE
    }

    public enum NotificationType{
        LIKE,
        COMMENT,
        FRIEND_REQUEST,
        FRIEND_ACCEPT,
        FRIEND_REJECT,
        SETTLEMENT_REQUEST,
        SETTLEMENT_PAYMENT,
        SETTLEMENT_CANCEL,
        SETTLEMENT_COMPLETE
    }

    public enum FriendRequestStatus{
        REQUEST,
        ACCEPT,
        REJECT,
        CANCEL
    }
}

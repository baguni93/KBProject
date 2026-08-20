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
        PAYMENT,
        LIKE,
        COMMENT,
        FRIEND_REQUEST,
        FRIEND_ACCEPT,
        FRIEND_REJECT,
        SETTLEMENT_REQUEST,
        SETTLEMENT_PAYMENT,
        SETTLEMENT_CANCEL,
        SETTLEMENT_CANCEL_REFUND,
        SETTLEMENT_COMPLETE,
        SETTLEMENT_COMPLETE_OWNER,
        SETTLEMENT_REMIND
    }
    public enum NotificationStatusType{
        READ,
        UNREAD,
    }

    public enum FriendRequestStatus{
        REQUEST,
        ACCEPT,
        REJECT,
        CANCEL
    }

    public enum FriendStatus{
        NONE,
        REQUEST,
        RECEIVED,
        FRIEND
    }


    public enum CheckCanIssueStatus{
        KB_ALREADY_HAS_CUSTOM,
        KB_AVAILABLE_FOR_CUSTOM,
        NOT_KB_BANK
    }

    public enum TaskType{
        ANALYSIS_COMPLETE,
        CARD_RECOMMEND,
        INSURANCE_RECOMMEND,
        EVENT_COMPLETE,
        PAYMENT_COMPLETE,
        PHONE_AUTH_SEND,
    }
}

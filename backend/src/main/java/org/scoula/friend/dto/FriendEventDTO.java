package org.scoula.friend.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FriendEventDTO {

    private String type;

    private int senderId;
    private int targetUserId;

    private Integer requestId;
}
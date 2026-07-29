package org.scoula.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.friend.domain.FriendVO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendResponseDTO {


    private int friendId;
    private int userId;
    private int friendUserId;
    private Date createdAt;

    private ProfileSimpleVO receiver;
    private ProfileSimpleVO sender;

    public static FriendResponseDTO of(FriendVO friendVO) {
        return friendVO == null ? null : FriendResponseDTO.builder()
                .friendId(friendVO.getFriendId())
                .userId(friendVO.getUserId())
                .friendUserId(friendVO.getFriendUserId())
                .createdAt(friendVO.getCreatedAt())
                .receiver(friendVO.getReceiver())
                .sender(friendVO.getSender())
                .build();

    }
}

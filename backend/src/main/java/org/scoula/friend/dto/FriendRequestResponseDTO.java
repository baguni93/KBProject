package org.scoula.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.friend.domain.FriendRequestVO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestResponseDTO {

    private int requestId;
    private int requesterId;
    private int receiverId;
    private Enum.FriendRequestStatus status;
    private Date createdAt;
    private Date updatedAt;

    private ProfileSimpleVO receiver;
    private ProfileSimpleVO sender;

    public static FriendRequestResponseDTO of(FriendRequestVO friendRequestVO) {

        return friendRequestVO == null ? null : FriendRequestResponseDTO.builder()
                .requestId(friendRequestVO.getRequestId())
                .requesterId(friendRequestVO.getRequesterId())
                .receiverId(friendRequestVO.getReceiverId())
                .status(friendRequestVO.getStatus())
                .createdAt(friendRequestVO.getCreatedAt())
                .updatedAt(friendRequestVO.getUpdatedAt())
                .receiver(friendRequestVO.getReceiver())
                .sender(friendRequestVO.getSender())
                .build();

    }
}

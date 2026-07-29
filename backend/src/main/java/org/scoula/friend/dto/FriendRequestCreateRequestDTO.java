package org.scoula.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.friend.domain.FriendRequestVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestCreateRequestDTO {

    private int requesterId;
    private int receiverId;

    public FriendRequestVO toVo() {
        return FriendRequestVO.builder()
                .requesterId(requesterId)
                .receiverId(receiverId)
                .build();

    }
}

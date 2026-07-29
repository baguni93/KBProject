package org.scoula.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import org.scoula.friend.domain.FriendVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendCreateRequestDTO {

    private int userId;
    private int friendUserId;

    public FriendVO toVo() {
        return FriendVO.builder()
                .userId(userId)
                .friendUserId(friendUserId)
                .build();

    }


}

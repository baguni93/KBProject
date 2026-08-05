package org.scoula.friend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.friend.domain.FriendStatusVO;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendStatusResponseDTO {

    private Integer requestId;
    private Enum.FriendStatus friendStatus;

    public static FriendStatusResponseDTO toVo(FriendStatusVO vo) {
        return  vo == null ? null : FriendStatusResponseDTO.builder()
                .requestId(vo.getRequestId())
                .friendStatus(vo.getFriendStatus())
                .build();
    }
}

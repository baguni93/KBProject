package org.scoula.friend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendStatusVO {

    private Enum.FriendStatus friendStatus;

    private Integer requestId;
}
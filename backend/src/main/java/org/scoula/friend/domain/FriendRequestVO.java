package org.scoula.friend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.ProfileSimpleVO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendRequestVO {

    private int requestId;
    private int requesterId;
    private int receiverId;
    private Enum.FriendRequestStatus status;
    private Date createdAt;
    private Date updatedAt;

    private ProfileSimpleVO receiver;
    private ProfileSimpleVO sender;
}

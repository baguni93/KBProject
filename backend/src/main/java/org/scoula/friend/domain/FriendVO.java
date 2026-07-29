package org.scoula.friend.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.feed.domain.ProfileSimpleVO;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FriendVO {

    private int friendId;
    private int userId;
    private int friendUserId;
    private Date createdAt;

    private ProfileSimpleVO receiver;
    private ProfileSimpleVO sender;
}

package org.scoula.like.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LikeNotificationActorVO {

    private int groupId;

    private int userId;

    private Date createdAt;
}
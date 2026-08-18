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
public class LikeNotificationGroupVO {

    private int groupId;

    private int feedId;

    private int receiverId;

    private int actorCount;

    private String status;

    private Date createdAt;

    private Date completedAt;
    private String notificationType;
}
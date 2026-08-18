package org.scoula.notification.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationGroupVO {

    private int groupId;

    private int receiverId;

    private int feedId;

    private String notificationType;

    private int actorCount;

    private String status;

    private Date createdAt;

    private Date completedAt;
}
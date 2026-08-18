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
public class NotificationGroupActorVO {

    private int groupId;

    private int userId;

    private Date createdAt;
}
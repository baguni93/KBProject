package org.scoula.notification.domain;

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
public class NotificationVO {

    private int notificationId;
    private int receiverId;
    private int senderId;
    private Enum.NotificationType notificationType;
    private int targetId;
    private Enum.NotificationStatusType status;
    private Date createdAt;
    private ProfileSimpleVO sender;
}

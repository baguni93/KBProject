package org.scoula.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.ProfileSimpleVO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.notification.domain.NotificationVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private int notificationId;
    private int receiverId;
    private int senderId;
    private Enum.NotificationType notificationType;
    private Enum.NotificationStatusType status;
    private int targetId;
    private Date createdAt;
    private ProfileSimpleVO sender;
    private int actorCount;

    public static NotificationResponseDTO of(NotificationVO notificationVO){

        return notificationVO == null? null : NotificationResponseDTO.builder()
                .notificationId(notificationVO.getNotificationId())
                .receiverId(notificationVO.getReceiverId())
                .senderId(notificationVO.getSenderId())
                .sender(notificationVO.getSender())
                .status(notificationVO.getStatus())
                .notificationType(notificationVO.getNotificationType())
                .targetId(notificationVO.getTargetId())
                .createdAt(notificationVO.getCreatedAt())
                .actorCount(notificationVO.getActorCount())
                .build();
    }
}

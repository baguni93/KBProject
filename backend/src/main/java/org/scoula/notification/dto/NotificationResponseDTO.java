package org.scoula.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.notification.domain.NotificationVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationResponseDTO {

    private int receiverId;
    private int senderId;
    private Enum.NotificationType notificationType;
    private int targetId;
    private Date createdAt;

    public static NotificationResponseDTO of(NotificationVO notificationVO){

        return notificationVO == null? null : NotificationResponseDTO.builder()
                .receiverId(notificationVO.getReceiverId())
                .senderId(notificationVO.getSenderId())
                .notificationType(notificationVO.getNotificationType())
                .targetId(notificationVO.getTargetId())
                .createdAt(notificationVO.getCreatedAt())
                .build();
    }
}

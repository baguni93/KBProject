package org.scoula.notification.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.notification.domain.NotificationVO;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NotificationRequestDTO {

    private int receiverId;
    private int senderId;
    private Enum.NotificationType notificationType;
    private int targetId;
    private int actorCount;


    public NotificationVO toVo(){
        return NotificationVO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(notificationType)
                .targetId(targetId)
                .actorCount(actorCount)
                .build();
    }

}

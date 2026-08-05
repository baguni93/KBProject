package org.scoula.notification.service;

import org.apache.ibatis.annotations.Param;
import org.scoula.common.util.Enum;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface NotificationService {


    NotificationResponseDTO create(NotificationRequestDTO notificationRequestDTO);
    NotificationResponseDTO get(int notificationId);

    List<NotificationResponseDTO> getList(int userId);

    void read(int notificationId);

    void readAll(int userId);

    void createLikeNotification(int senderId, int receiverId, Integer targetId);

    void createCommentNotification(int senderId, int receiverId, Integer targetId);

    void createFriendRequestNotification(int senderId, int receiverId, Integer targetId);

    void createFriendAcceptNotification(int senderId, int receiverId, Integer targetId);

    void createSettlementNotification(int senderId, int receiverId, Integer targetId, Enum.NotificationType type);

}

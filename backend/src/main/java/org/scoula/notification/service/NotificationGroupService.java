package org.scoula.notification.service;

import org.scoula.common.util.Enum;

public interface NotificationGroupService {

    void addActor(
            int feedId,
            int senderId,
            int receiverId,
            Enum.NotificationType notificationType
    );

    void removeActor(
            int feedId,
            int userId,
            int receiverId,
            Enum.NotificationType notificationType
    );
}
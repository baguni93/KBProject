package org.scoula.notification.service;

import org.apache.ibatis.annotations.Param;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;

import java.util.List;

public interface NotificationService {
    NotificationResponseDTO create(NotificationRequestDTO notificationRequestDTO);
    NotificationResponseDTO get(int notificationId);

    List<NotificationResponseDTO> getList(int userId);

    NotificationResponseDTO read(int notificationId);

    List<NotificationResponseDTO> readAll(int userId);
}

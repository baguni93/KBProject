package org.scoula.notification.mapper;

import org.scoula.notification.domain.NotificationVO;

import java.util.List;

public interface NotificationMapper {
    void create(NotificationVO notificationVO);

    NotificationVO get(int notificationId);

    void read(int notificationId);

    void readAll(int userId);

    List<NotificationVO> getList(int userId);
}

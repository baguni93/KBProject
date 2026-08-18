package org.scoula.notification.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.notification.domain.NotificationVO;

import java.util.List;

public interface NotificationMapper {
    void create(NotificationVO notificationVO);

    NotificationVO get(int notificationId);

    void read(int notificationId);

    void readAll(int userId);

    List<NotificationVO> getList(int userId);

    boolean existsLikeNotification(@Param("userId") int userId,
                                   @Param("receiverId") int receiverId,
                                   @Param("feedId") int feedId);


    boolean existsFriendRequestNotification(@Param("userId") int senderId,
                                            @Param("receiverId")int receiverId,
                                            @Param("targetId") int targetId);


}

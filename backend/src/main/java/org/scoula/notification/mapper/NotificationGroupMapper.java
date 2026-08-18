package org.scoula.notification.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.notification.domain.NotificationGroupActorVO;
import org.scoula.notification.domain.NotificationGroupVO;

public interface NotificationGroupMapper {

    NotificationGroupVO findWaitingGroup(
            @Param("feedId") int feedId,
            @Param("receiverId") int receiverId,
            @Param("notificationType") String notificationType
    );

    void createGroup(
            NotificationGroupVO group
    );

    int createActor(
            NotificationGroupActorVO actor
    );

    void increaseActorCount(
            @Param("groupId") int groupId
    );

    void decreaseActorCount(
            @Param("groupId") int groupId
    );

    int getActorCount(
            @Param("groupId") int groupId
    );

    Integer getFirstActor(
            @Param("groupId") int groupId
    );

    int deleteActor(
            @Param("groupId") int groupId,
            @Param("userId") int userId
    );

    void completeGroup(
            @Param("groupId") int groupId
    );

    void deleteGroup(
            @Param("groupId") int groupId
    );
}
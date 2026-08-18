package org.scoula.like.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.like.domain.LikeNotificationActorVO;
import org.scoula.like.domain.LikeNotificationGroupVO;

public interface LikeMapper {

    /* =========================
     * 좋아요
     * ========================= */

    boolean exists(
            @Param("feedId") int feedId,
            @Param("userId") int userId
    );

    void create(
            @Param("feedId") int feedId,
            @Param("userId") int userId
    );

    void delete(
            @Param("feedId") int feedId,
            @Param("userId") int userId
    );

    int getLikeCount(
            @Param("feedId") int feedId
    );

    int getFeedOwner(
            @Param("feedId") int feedId
    );


}
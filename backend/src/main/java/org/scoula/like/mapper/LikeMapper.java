package org.scoula.like.mapper;

import org.apache.ibatis.annotations.Param;

public interface LikeMapper {

    boolean exists(@Param("feedId") int feedId,
               @Param("userId") int userId);
    void create(@Param("feedId") int feedId,
                @Param("userId") int userId);
    void delete(@Param("feedId") int feedId,
                @Param("userId") int userId);

    int getLikeCount(int feedId);

    int getFeedOwner(int feedId);
}

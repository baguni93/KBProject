package org.scoula.like.mapper;

import org.apache.ibatis.annotations.Param;

public interface LikeMapper {
    void create(@Param("feedId") int feedId,
                @Param("userId") int userId);
    void delete(@Param("feedId") int feedId,
                @Param("userId") int userId);
}

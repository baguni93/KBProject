package org.scoula.auth.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AuthMapper {
    String getPinPasswordByUserId(@Param("userId") Integer userId);
}

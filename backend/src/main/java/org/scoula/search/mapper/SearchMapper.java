package org.scoula.search.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchMapper {
    List<SearchResponseDTO> searchMember(@Param("keyword") String keyword , @Param("userId") int userId);
}

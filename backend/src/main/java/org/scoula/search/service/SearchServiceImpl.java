package org.scoula.search.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.search.dto.SearchResponseDTO;
import org.scoula.search.mapper.SearchMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class SearchServiceImpl implements SearchService {

    private final SearchMapper searchMapper;

    @Override
    public List<SearchResponseDTO> search(String keyword , int userId) {
        return searchMapper.searchMember(keyword , userId);
    }
}

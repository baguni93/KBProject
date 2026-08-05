package org.scoula.search.service;

import org.scoula.search.dto.SearchResponseDTO;

import java.util.List;

public interface SearchService {

    List<SearchResponseDTO> search(String keyword ,int userId);
}

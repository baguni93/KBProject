package org.scoula.search.controller;

import lombok.RequiredArgsConstructor;
import org.scoula.search.dto.SearchResponseDTO;
import org.scoula.search.service.SearchService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/member/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<List<SearchResponseDTO>> search(
            @RequestParam String keyword,
            @RequestParam int userId){
        return ResponseEntity.ok(searchService.search(keyword, userId));
    }


}

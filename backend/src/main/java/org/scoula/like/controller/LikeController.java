package org.scoula.like.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.like.service.LikeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/like")
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PostMapping("/{feedId}")
    public ResponseEntity<HttpStatus> create(@PathVariable int feedId, @RequestParam int userId){
        likeService.create(feedId, userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @DeleteMapping("/{feedId}")
    public ResponseEntity<HttpStatus> delete(@PathVariable int feedId , @RequestParam int userId)
    {
        likeService.delete(feedId, userId);
        return ResponseEntity.ok(HttpStatus.OK);
    }


}

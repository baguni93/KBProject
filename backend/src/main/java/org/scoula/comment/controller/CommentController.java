package org.scoula.comment.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.comment.dto.CommentRequestDTO;
import org.scoula.comment.dto.CommentResponseDTO;
import org.scoula.comment.service.CommentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Log4j2
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    private ResponseEntity<CommentResponseDTO> create(@RequestBody CommentRequestDTO commentRequestDTO){
        return ResponseEntity.ok(commentService.create(commentRequestDTO));
    }

    @GetMapping("/{feedId}")
    private ResponseEntity<List<CommentResponseDTO>>  getList(@PathVariable int feedId, @RequestParam int userId){
        return ResponseEntity.ok(commentService.getList(feedId, userId));
    }

    @DeleteMapping("/{commentId}")
    private ResponseEntity<HttpStatus> delete(@PathVariable int commentId){
        commentService.delete(commentId);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @PatchMapping
    private ResponseEntity<CommentResponseDTO> update(@RequestBody CommentRequestDTO commentRequestDTO){
        return ResponseEntity.ok(commentService.update(commentRequestDTO));
    }



}

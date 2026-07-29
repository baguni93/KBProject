package org.scoula.comment.service;

import org.scoula.comment.dto.CommentRequestDTO;
import org.scoula.comment.dto.CommentResponseDTO;

import java.util.List;

public interface CommentService {
    CommentResponseDTO create(CommentRequestDTO commentRequestDTO);
    CommentResponseDTO get(int commentId , int userId);

    List<CommentResponseDTO> getList(int feedId , int userId);

    void delete(int commentId);

    CommentResponseDTO update(CommentRequestDTO commentRequestDTO);
}

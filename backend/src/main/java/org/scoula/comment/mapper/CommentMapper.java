package org.scoula.comment.mapper;

import org.scoula.comment.domain.CommentVO;

import java.util.List;

public interface CommentMapper {
    void create(CommentVO vo);

    CommentVO get(int commentId);

    List<CommentVO> getList(int feedId);

    void delete(int commentId);

    void update(CommentVO vo);
}

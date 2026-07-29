package org.scoula.comment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.comment.domain.CommentVO;
import org.scoula.comment.dto.CommentRequestDTO;
import org.scoula.comment.dto.CommentResponseDTO;
import org.scoula.comment.mapper.CommentMapper;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Log4j2
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService{

    private final CommentMapper commentMapper;

    @Transactional
    @Override
    public CommentResponseDTO create(CommentRequestDTO commentRequestDTO) {

        CommentVO vo = commentRequestDTO.toVo();

        commentMapper.create(vo);

        return get(vo.getCommentId() , vo.getUserId());
    }

    @Transactional
    @Override
    public CommentResponseDTO get(int commentId, int userId) {

        CommentVO vo = commentMapper.get(commentId);

        return CommentResponseDTO.of(vo , userId);
    }

    @Override
    public List<CommentResponseDTO> getList(int feedId ,int userId) {

        List<CommentVO> list = commentMapper.getList(feedId);

        return list.stream().map(x -> {
            return CommentResponseDTO.of(x , x.getUserId());
        }).toList();
    }

    @Transactional
    @Override
    public void delete(int commentId) {

        CommentVO commentVO = commentMapper.get(commentId);

        if(commentVO == null){
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        commentMapper.delete(commentId);
    }

    @Transactional
    @Override
    public CommentResponseDTO update(CommentRequestDTO commentRequestDTO) {

        CommentVO vo = commentRequestDTO.toVo();

        if(commentMapper.get(vo.getCommentId()) == null){
            throw new CustomException(ErrorCode.COMMENT_NOT_FOUND);
        }

        commentMapper.update(vo);

        return get(vo.getCommentId() , vo.getUserId());
    }
}

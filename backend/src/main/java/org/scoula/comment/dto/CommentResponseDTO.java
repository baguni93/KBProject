package org.scoula.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.comment.domain.CommentVO;
import org.scoula.feed.domain.ProfileSimpleVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentResponseDTO {

    private int commentId;
    private int feedId;
    private int userId;
    private String content;
    private Date createdAt;
    private Date updatedAt;

    private ProfileSimpleVO writer;
    private boolean isMine;

    public static CommentResponseDTO of(CommentVO vo , int userId) {
        return vo == null ? null : CommentResponseDTO.builder()
                .commentId(vo.getCommentId())
                .feedId(vo.getFeedId())
                .userId(vo.getUserId())
                .content(vo.getContent())
                .createdAt(vo.getCreatedAt())
                .updatedAt(vo.getUpdatedAt())
                .writer(vo.getWriter())
                .isMine(vo.getUserId() == userId)
                .build();
    }
}

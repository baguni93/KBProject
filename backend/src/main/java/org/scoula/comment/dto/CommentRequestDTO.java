package org.scoula.comment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.comment.domain.CommentVO;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentRequestDTO {


    private int feedId;
    private int userId;
    private String content;

    public CommentVO toVo() {
        return  CommentVO.builder()
                .feedId(feedId)
                .userId(userId)
                .content(content)
                .build();

    }
}

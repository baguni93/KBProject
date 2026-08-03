package org.scoula.comment.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.feed.domain.ProfileSimpleVO;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommentVO {

    private int commentId;
    private int feedId;
    private int feedOwnerId;
    private int userId;
    private String content;
    private Date createdAt;
    private Date updatedAt;

    private ProfileSimpleVO writer;

}

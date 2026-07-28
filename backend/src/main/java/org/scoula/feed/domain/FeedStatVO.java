package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FeedStatVO {

    private int likeCount;
    private int commentCount;
}

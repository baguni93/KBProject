package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedVO {

    private int feedId;
    private int userId;
    private int transactionId;
    private String feedType;
    private String content;
    private String visibility;
    private Date createdAt;
    private Date updatedAt;

    private FeedStatVO stat;
    private List<FeedImageVO> images;
    private TransactionVO transaction;
    private ProfileSimpleVO sender;
    private ProfileSimpleVO receiver;
    private CategoryVO category;
}

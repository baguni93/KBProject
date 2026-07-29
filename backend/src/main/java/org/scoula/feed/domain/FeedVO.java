package org.scoula.feed.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.common.util.Enum;
import org.scoula.settlement.domain.SettlementVO;

import java.util.Date;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FeedVO {

    private int feedId;

    private int userId;
    private int targetId;

    private Enum.FeedType feedType;

    private String content;
    private Enum.VisibilityType visibility;

    private Date createdAt;
    private Date updatedAt;

    private FeedStatVO stat;
    private List<FeedImageVO> images;

    private ProfileSimpleVO sender;

    private TransactionVO transaction;
    private SettlementVO settlement;

    private CardVO card;
    private AnalysisVO analysis;
    private EventVO event;
}

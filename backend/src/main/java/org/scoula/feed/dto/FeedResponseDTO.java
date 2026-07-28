package org.scoula.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.ibatis.jdbc.Null;
import org.scoula.common.util.Enum;
import org.scoula.feed.domain.*;
import org.scoula.settlement.domain.SettlementVO;

import java.util.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponseDTO {

    // 피드 정보
    private int feedId;

    private int userId;

    private int targetId;

    private Enum.FeedType  feedType;

    private String content;

    private Enum.VisibilityType visibility;

    // 생성 정보
    private Date createdAt;
    private Date updatedAt;

    // 통계
    private int likeCount;
    private int commentCount;

    // 사용자 정보
    private ProfileSimpleVO sender;
    //거래 정보
    private TransactionVO transaction;
    //카드 이미지
    private CardVO card;
    //소비패턴 분석
    private AnalysisVO analysis;
    //이벤트
    private EventVO event;
    //정산
    private SettlementVO settlement;

    // 이미지
    private List<FeedImageDTO> images;

    public static FeedResponseDTO of(FeedVO feedVO){

    return feedVO == null? null : FeedResponseDTO.builder()
            .feedId(feedVO.getFeedId())
            .userId(feedVO.getUserId())
            .targetId(feedVO.getTargetId())
            .feedType(feedVO.getFeedType())
            .content(feedVO.getContent())
            .visibility(feedVO.getVisibility())
            .createdAt(feedVO.getCreatedAt())
            .updatedAt(feedVO.getUpdatedAt())
            .likeCount(feedVO.getStat().getLikeCount())
            .commentCount(feedVO.getStat().getCommentCount())
            .sender(feedVO.getSender())
            .settlement(feedVO.getSettlement())
            .transaction(feedVO.getTransaction())
            .card(feedVO.getCard())
            .analysis(feedVO.getAnalysis())
            .event(feedVO.getEvent())
            .build();
    }
}

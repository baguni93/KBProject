package org.scoula.feed.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.scoula.feed.domain.FeedVO;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeedResponseDTO {

    // 피드 정보
    private int feedId;

    private int userId;

    private int transactionId;

    private String feedType;

    private String content;

    private String visibility;

    // 생성 정보
    private Date createdAt;

    private Date updatedAt;

    //거래 정보
    private String transactionType;

    //카테고리
    private String spendingCategory;

    // 사용자 정보
    private String senderNickname;
    private String senderProfileImage;

    private String receiverNickname;
    private String receiverProfileImage;


    // 통계
    private int likeCount;

    private int commentCount;


    // 이미지
    private List<FeedImageDTO> images;


        public static FeedResponseDTO of(FeedVO feedVO){

        return feedVO == null? null : FeedResponseDTO.builder()
                .feedId(feedVO.getFeedId())
                .userId(feedVO.getUserId())
                .transactionId(feedVO.getTransactionId())
                .feedType(feedVO.getFeedType())
                .content(feedVO.getContent())
                .visibility(feedVO.getVisibility())
                .createdAt(feedVO.getCreatedAt())
                .updatedAt(feedVO.getUpdatedAt())
                .spendingCategory(feedVO.getCategory().getCategoryName())
                .senderNickname(feedVO.getSender().getNickname())
                .senderProfileImage(feedVO.getSender().getProfileImageName())
                .receiverNickname(feedVO.getReceiver().getNickname())
                .receiverProfileImage(feedVO.getReceiver().getProfileImageName())
                .transactionType(feedVO.getTransaction().getTransactionType())
                .likeCount(feedVO.getStat().getLikeCount())
                .commentCount(feedVO.getStat().getCommentCount())
                .build();
    }


}

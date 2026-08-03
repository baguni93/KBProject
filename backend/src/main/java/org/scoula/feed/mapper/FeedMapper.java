package org.scoula.feed.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.feed.domain.*;
import org.scoula.settlement.domain.SettlementVO;

import java.util.List;

public interface FeedMapper {

    List<FeedVO> getList(int userId);
    List<FeedVO> getMyList(int userId);
    List<FeedVO> getFriendList(int userId);

    void create(FeedVO feedVO);
    void delete(int feedId);

    FeedImageVO getImage(int imageId);
    void createFeedImage(FeedImageVO feedImageVO);
    List<FeedImageVO> getImages(int feedId); // 해당 이미지 목록 얻기

    FeedVO getFeedCommon(Integer feedId);

    TransactionVO getTransaction(int targetId);

    CardVO getCard(int targetId);

    AnalysisVO getAnalysis(int targetId);

    EventVO getEvent(int targetId);

    List<FeedVO> geMemberList(@Param("memberUserId") int memberUserId,
                              @Param("userId") int userId);

}

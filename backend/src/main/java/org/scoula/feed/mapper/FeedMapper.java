package org.scoula.feed.mapper;

import org.scoula.feed.domain.FeedImageVO;
import org.scoula.feed.domain.FeedVO;
import org.scoula.feed.domain.TransactionVO;

import java.util.List;

public interface FeedMapper {

    List<FeedVO> getList();
    void create(FeedVO feedVO);
    void delete(int feedId);
    FeedVO get(int feedId);

    FeedImageVO getImage(int imageId);
    void createFeedImage(FeedImageVO feedImageVO);
    List<FeedImageVO> getImages(int feedId); // 해당 이미지 목록 얻기

    List<FeedVO> getMyList(int userId);

    List<FeedVO> getFriendList(int userId);

}

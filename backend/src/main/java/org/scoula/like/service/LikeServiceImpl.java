package org.scoula.like.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.feed.domain.FeedVO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.mapper.FeedMapper;
import org.scoula.feed.service.FeedService;
import org.scoula.like.mapper.LikeMapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class LikeServiceImpl implements  LikeService {
    private final LikeMapper likeMapper;
    private final FeedMapper feedMapper;
    @Override
    public void create(int feedId , int userId) {

        validateFeed(feedId);
        likeMapper.create(feedId ,userId);
    }

    @Override
    public void delete(int feedId, int userId) {

        validateFeed(feedId);
        likeMapper.delete(feedId ,userId);
    }


    private void validateFeed(int feedId){

        FeedVO feedVO = feedMapper.getFeedCommon(feedId);

        if(feedVO == null){
            throw new CustomException(ErrorCode.FEED_NOT_FOUND);
        }
    }
}

package org.scoula.feed.service;

import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.dto.FeedImageDTO;
import org.scoula.feed.dto.FeedResponseDTO;
import org.scoula.feed.dto.FeedUpdateRequestDTO;

import java.util.List;

public interface FeedService {

     FeedResponseDTO create(FeedCreateRequestDTO request);
     FeedResponseDTO get(int feedId ,int userId);
     List<FeedResponseDTO> getList(int userId ,int page, int size);
     List<FeedResponseDTO> getFriendList(int userId);
     List<FeedResponseDTO> getMyList(int userId ,int page, int size);
     FeedResponseDTO delete(int feedId ,int userId);

     FeedImageDTO getImage(int imageId);

    List<FeedResponseDTO> geMemberList(int memberUserId, int userId ,int page, int size);

    void updateFeed(FeedUpdateRequestDTO feedUpdateRequestDTO);
}

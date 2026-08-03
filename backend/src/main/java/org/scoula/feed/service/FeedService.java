package org.scoula.feed.service;

import org.scoula.feed.dto.FeedCreateRequestDTO;
import org.scoula.feed.dto.FeedImageDTO;
import org.scoula.feed.dto.FeedResponseDTO;

import java.util.List;

public interface FeedService {

     FeedResponseDTO create(FeedCreateRequestDTO request);
     FeedResponseDTO get(int feedId);
     List<FeedResponseDTO> getList(int userId);
     List<FeedResponseDTO> getFriendList(int userId);
     List<FeedResponseDTO> getMyList(int userId);
     FeedResponseDTO delete(int feedId);

     FeedImageDTO getImage(int imageId);

    List<FeedResponseDTO> geMemberList(int memberUserId, int userId);
}

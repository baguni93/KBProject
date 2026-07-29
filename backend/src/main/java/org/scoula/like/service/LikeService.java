package org.scoula.like.service;

import org.scoula.feed.dto.FeedResponseDTO;

import java.util.List;

public interface LikeService {
    void create(int feedId , int userId);

    void delete(int feedId, int userId);
}

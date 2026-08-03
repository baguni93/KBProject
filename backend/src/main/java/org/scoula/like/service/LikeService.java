package org.scoula.like.service;

import org.scoula.feed.dto.FeedResponseDTO;

import java.util.List;

public interface LikeService {
    boolean toggle(int feedId, int userId);
    void decreaseLikeCount(int feedId);
    void increaseLikeCount(int feedId);
    boolean isLiked(int feedId, int userId);
    int getLikeCount(int feedId);
}

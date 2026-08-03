package org.scoula.friend.mapper;

import org.apache.ibatis.annotations.Param;
import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.domain.FriendStatusVO;
import org.scoula.friend.domain.FriendVO;

import java.util.List;

public interface FriendMapper {
    List<FriendVO> getList(int userId);

    List<FriendRequestVO> getRequestList(int userId);

    void deleteFriend(@Param("friendUserId") int friendUserId,
                      @Param("userId") int  userId);

    void createRequest(FriendRequestVO friendRequestVO);

    FriendRequestVO getRequest(int requestId);

    FriendVO getFriend(int friendId);

    void createFriend(FriendVO friendVO);

    List<FriendVO> getFriends(@Param("friendUserId") int friendUserId,
                              @Param("userId") int  userId);

    List<FriendRequestVO> getSendRequestList(int userId);

    void deleteRequest(FriendRequestVO friendRequestVO);

    FriendStatusVO getFriendStatus(@Param("userId") int userId,
                                @Param("checkUserId") int checkUserId);

}

package org.scoula.friend.service;

import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.dto.*;

import java.util.List;

public interface FriendService {
    List<FriendResponseDTO> getList(int userId);

    List<FriendRequestResponseDTO> getRequestList(int userId);

    List<FriendResponseDTO> deleteFriend(int friendUserId, int userId);
    FriendResponseDTO createFriend(FriendCreateRequestDTO request);

    FriendRequestResponseDTO createRequest(FriendRequestCreateRequestDTO request);

    void acceptRequest(int requestId);

    void cancelRequest(int requestId);

    void rejectRequest(int requestId);

    FriendRequestResponseDTO getRequest(int requestId);

    FriendResponseDTO getFriend(int friendId);

    List<FriendRequestResponseDTO> getsendRequestList(int userId);

    FriendStatusResponseDTO getFriendStatus(int checkUserId, int userId);
}

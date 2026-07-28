package org.scoula.friend.service;

import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.dto.FriendCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestResponseDTO;
import org.scoula.friend.dto.FriendResponseDTO;

import java.util.List;

public interface FriendService {
    List<FriendResponseDTO> getList(int userId);

    List<FriendRequestResponseDTO> getRequestList(int userId);

    List<FriendResponseDTO> deleteFriend(int friendUserId, int userId);
    FriendResponseDTO createFriend(FriendCreateRequestDTO request);

    FriendRequestResponseDTO createRequest(FriendRequestCreateRequestDTO request);

    FriendRequestResponseDTO acceptRequest(int requestId);

    FriendRequestResponseDTO cancelRequest(int requestId);

    FriendRequestResponseDTO rejectRequest(int requestId);

    FriendRequestResponseDTO getRequest(int requestId);

    FriendResponseDTO getFriend(int friendId);
}

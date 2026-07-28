package org.scoula.friend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.domain.FriendVO;
import org.scoula.friend.dto.FriendCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestCreateRequestDTO;
import org.scoula.friend.dto.FriendRequestResponseDTO;
import org.scoula.friend.dto.FriendResponseDTO;
import org.scoula.friend.mapper.FriendMapper;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.service.NotificationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FriendServiceImpl implements FriendService{

   private final FriendMapper friendMapper;
   private final NotificationService notificationService;

    @Override
    public List<FriendResponseDTO> getList(int userId) {

        List<FriendVO> list = friendMapper.getList(userId);

        return list.stream().map(FriendResponseDTO::of).toList();
    }

    @Override
    public List<FriendRequestResponseDTO> getRequestList(int userId) {

        List<FriendRequestVO> list = friendMapper.getRequestList(userId);

        return list.stream().map(FriendRequestResponseDTO::of).toList();
    }

    @Transactional
    @Override
    public List<FriendResponseDTO> deleteFriend(int friendUserId, int userId) {
        //친구 삭제
        List<FriendVO> list =  friendMapper.getFriends(friendUserId , userId);

        if(list ==null || list.isEmpty()) {
            throw new CustomException(ErrorCode.FRIEND_NOT_FOUND);
        }

        friendMapper.deleteFriend(friendUserId, userId);

        return list.stream().map(FriendResponseDTO::of).toList();
    }

    @Transactional
    @Override
    public FriendResponseDTO createFriend(FriendCreateRequestDTO request) {

        FriendVO friendVO = request.toVo();

        friendMapper.createFriend(friendVO);

        return getFriend(friendVO.getFriendId());
    }

    @Transactional
    @Override
    public FriendResponseDTO getFriend(int friendId) {

        FriendVO friendVO =  friendMapper.getFriend(friendId);

        if(friendVO ==null) {
            throw new CustomException(ErrorCode.FRIEND_NOT_FOUND);
        }

        return FriendResponseDTO.of(friendVO);
    }


    @Transactional
    @Override
    public FriendRequestResponseDTO createRequest(FriendRequestCreateRequestDTO request) {

        FriendRequestVO friendRequestVO = request.toVo();


        try {
            friendMapper.createRequest(friendRequestVO);
        } catch (DuplicateKeyException e) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_ALREADY_EXISTS);
        }


        notificationService.create(NotificationRequestDTO.builder()
                .receiverId(friendRequestVO.getRequesterId())
                .senderId(friendRequestVO.getReceiverId())
                .notificationType(Enum.NotificationType.FRIEND_REQUEST)
                .targetId(friendRequestVO.getRequestId())
                .build());

        return getRequest(friendRequestVO.getRequestId());
    }


    @Override
    public FriendRequestResponseDTO getRequest(int requestId) {

        FriendRequestVO friendRequestVO = friendMapper.getRequest(requestId);

        if(friendRequestVO ==null) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        return FriendRequestResponseDTO.of(friendRequestVO);
    }


    @Transactional
    @Override
    public FriendRequestResponseDTO acceptRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.acceptRequest(friendRequestVO);

        notificationService.create(NotificationRequestDTO.builder()
                .receiverId(friendRequestVO.getReceiverId())
                .senderId(friendRequestVO.getRequesterId())
                .notificationType(Enum.NotificationType.FRIEND_ACCEPT)
                .targetId(friendRequestVO.getRequestId())
                .build());

        return  getRequest(friendRequestVO.getRequestId());
    }

    @Transactional
    @Override
    public FriendRequestResponseDTO cancelRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.cancelRequest(friendRequestVO);

        return  getRequest(friendRequestVO.getRequestId());
    }

    @Transactional
    @Override
    public FriendRequestResponseDTO rejectRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.rejectRequest(friendRequestVO);


        notificationService.create(NotificationRequestDTO.builder()
                .receiverId(friendRequestVO.getReceiverId())
                .senderId(friendRequestVO.getRequesterId())
                .notificationType(Enum.NotificationType.FRIEND_REJECT)
                .targetId(friendRequestVO.getRequestId())
                .build());

        return  getRequest(friendRequestVO.getRequestId());
    }

    private FriendRequestVO validateFriendRequest(int requestId){

        FriendRequestVO friendRequestVO = friendMapper.getRequest(requestId);

        if(friendRequestVO ==null) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }

        if(friendRequestVO.getStatus() == Enum.FriendRequestStatus.ACCEPT){
            throw new CustomException(ErrorCode.FRIEND_ALREADY_ACCEPT);
        }
        if(friendRequestVO.getStatus() == Enum.FriendRequestStatus.REJECT){
            throw new CustomException(ErrorCode.FRIEND_ALREADY_REJECT);
        }
        if(friendRequestVO.getStatus() == Enum.FriendRequestStatus.CANCEL){
            throw new CustomException(ErrorCode.FRIEND_ALREADY_CANCEL);
        }

        return friendRequestVO;

    }

}

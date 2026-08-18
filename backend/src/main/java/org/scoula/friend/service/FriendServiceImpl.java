package org.scoula.friend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.friend.domain.FriendRequestVO;
import org.scoula.friend.domain.FriendStatusVO;
import org.scoula.friend.domain.FriendVO;
import org.scoula.friend.dto.*;
import org.scoula.friend.mapper.FriendMapper;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.service.NotificationService;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class FriendServiceImpl implements FriendService{

   private final FriendMapper friendMapper;
   private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

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


    @Override
    public List<FriendRequestResponseDTO> getsendRequestList(int userId) {
        List<FriendRequestVO> list = friendMapper.getSendRequestList(userId);

        return list.stream().map(FriendRequestResponseDTO::of).toList();
    }

    @Override
    public FriendStatusResponseDTO getFriendStatus(int checkUserId, int userId) {

        FriendStatusVO vo = friendMapper.getFriendStatus(userId, checkUserId);

        return FriendStatusResponseDTO.toVo(vo);

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

        sendFriendEvent(
                friendUserId,
                "FRIEND_DELETE",
                userId,
                null
        );

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

        notificationService.createFriendRequestNotification(
                friendRequestVO.getRequesterId(),
                friendRequestVO.getReceiverId(),
                friendRequestVO.getRequestId()
        );

        sendFriendEvent(
                friendRequestVO.getReceiverId(),
                "FRIEND_REQUEST",
                friendRequestVO.getRequesterId(),
                friendRequestVO.getRequestId()
        );


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
    public void acceptRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.deleteRequest(friendRequestVO);

        friendMapper.createFriend(FriendCreateRequestDTO.builder()
                .userId(friendRequestVO.getRequesterId())
                .friendUserId(friendRequestVO.getReceiverId())
                .build().toVo());


        notificationService.createFriendAcceptNotification(
                friendRequestVO.getReceiverId(),
                friendRequestVO.getRequesterId(),
                friendRequestVO.getRequestId()
        );

        sendFriendEvent(
                friendRequestVO.getRequesterId(),
                "FRIEND_ACCEPT",
                friendRequestVO.getReceiverId(),
                friendRequestVO.getRequestId()
        );

    }

    @Transactional
    @Override
    public void cancelRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.deleteRequest(friendRequestVO);

        sendFriendEvent(
                friendRequestVO.getReceiverId(),
                "FRIEND_CANCEL",
                friendRequestVO.getRequesterId(),
                friendRequestVO.getRequestId()
        );

    }

    @Transactional
    @Override
    public void rejectRequest(int requestId) {

        FriendRequestVO friendRequestVO = validateFriendRequest(requestId);

        friendMapper.deleteRequest(friendRequestVO);

        sendFriendEvent(
                friendRequestVO.getRequesterId(),
                "FRIEND_REJECT",
                friendRequestVO.getReceiverId(),
                friendRequestVO.getRequestId()
        );

    }

    private FriendRequestVO validateFriendRequest(int requestId){

        FriendRequestVO friendRequestVO = friendMapper.getRequest(requestId);

        if(friendRequestVO ==null) {
            throw new CustomException(ErrorCode.FRIEND_REQUEST_NOT_FOUND);
        }
        return friendRequestVO;

    }

    private void sendFriendEvent(
            int receiverId,
            String type,
            int senderId,
            Integer requestId
    ) {

        FriendEventDTO event = FriendEventDTO.builder()
                .type(type)
                .senderId(senderId)
                .targetUserId(receiverId)
                .requestId(requestId)
                .build();

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/queue/friends",
                event
        );

        log.info(
                "친구 상태 웹소켓 전송 - receiverId: {}, type: {}",
                receiverId,
                type
        );
    }

}

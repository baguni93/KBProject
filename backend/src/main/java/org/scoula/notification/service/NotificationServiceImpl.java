package org.scoula.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
import org.scoula.feed.domain.FeedVO;
import org.scoula.notification.domain.NotificationVO;
import org.scoula.notification.dto.NotificationRequestDTO;
import org.scoula.notification.dto.NotificationResponseDTO;
import org.scoula.notification.mapper.NotificationMapper;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
@Transactional
public class NotificationServiceImpl implements NotificationService{

   private final NotificationMapper notificationMapper;
   private final SimpMessagingTemplate messagingTemplate;

   //알림 정책 : "같은 대상에게 같은 종류의 알림을 이미 읽었다면 다시 보내지 않는다."

    @Override
    public void createCommentNotification(int senderId, int receiverId, Integer targetId) {

        if(receiverId != senderId){

            var requestDTO = NotificationRequestDTO.builder()
                    .receiverId(receiverId)
                    .senderId(senderId)
                    .notificationType(Enum.NotificationType.COMMENT)
                    .targetId(targetId)
                    .build();

            log.info("send user = {}", senderId);

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/notifications",
                    create(requestDTO)
            );
        }
    }

    @Override
    public void createFriendRequestNotification(int senderId, int receiverId, Integer targetId) {

        //읽지 않은 친구 요청이 있는 지 확인
        //읽었으면 재전송
        //안읽었으면 보류
        boolean exists =
                notificationMapper.existsFriendRequestNotification(
                        senderId,
                        receiverId,
                        targetId
                );

        log.info("알림 여부 : {}", exists);

        if(!exists) {

            var requestDTO = NotificationRequestDTO.builder()
                    .receiverId(receiverId)
                    .senderId(senderId)
                    .notificationType(Enum.NotificationType.FRIEND_REQUEST)
                    .targetId(targetId)
                    .build();

            log.info("send user = {}", senderId);

            messagingTemplate.convertAndSendToUser(
                    String.valueOf(receiverId),
                    "/queue/notifications",
                    create(requestDTO)
            );
        }
    }

    @Override
    public void createFriendAcceptNotification(int senderId, int receiverId, Integer targetId) {

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(Enum.NotificationType.FRIEND_ACCEPT)
                .targetId(targetId)
                .build();

        log.info("send user = {}", senderId);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/queue/notifications",
                create(requestDTO)
        );
    }

    @Override
    public void createSettlementNotification(int senderId,
                                             int receiverId,
                                             Integer targetId,
                                             Enum.NotificationType type) {

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(type)
                .targetId(targetId)
                .build();

        log.info("send user = {}", senderId);

        messagingTemplate.convertAndSendToUser(
                String.valueOf(receiverId),
                "/queue/notifications",
                create(requestDTO)
        );
    }



    @Override
   public void createLikeNotification(int senderId, int receiverId, Integer targetId){

       // 자기 자신 제외
       if(receiverId != senderId){


           boolean exists =
                   notificationMapper.existsLikeNotification(
                           senderId,
                           receiverId,
                           targetId
                   );

           log.info("알림 여부 : {}", exists);

           if(!exists){

               var requestDTO = NotificationRequestDTO.builder()
                       .receiverId(receiverId)
                       .senderId(senderId)
                       .notificationType(Enum.NotificationType.LIKE)
                       .targetId(targetId)
                       .build();

               log.info("send user = {}", senderId);

               messagingTemplate.convertAndSendToUser(
                       String.valueOf(receiverId),
                       "/queue/notifications",
                       create(requestDTO)
               );
           }
       }
   }


    @Override
    public NotificationResponseDTO create(NotificationRequestDTO request) {

        NotificationVO notificationVO = request.toVo();

        notificationMapper.create(notificationVO);
        return get(notificationVO.getNotificationId());
    }


    @Override
    public NotificationResponseDTO get(int notificationId) {

       //관리자용 메서드
       NotificationVO notificationVO = notificationMapper.get(notificationId);

        return NotificationResponseDTO.of(notificationVO);
    }

    @Override
    public List<NotificationResponseDTO> getList(int userId) {

       List<NotificationVO> list = notificationMapper.getList(userId);

        return list.stream().map(NotificationResponseDTO::of).toList();
    }

    @Transactional
    @Override
    public void read(int notificationId) {

        validate(notificationId);
        notificationMapper.read(notificationId);
    }

    @Override
    public void readAll(int userId) {
        notificationMapper.readAll(userId);
    }


    private void validate(int targetId){

        var vo = get(targetId);

        if(vo == null){
            throw new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND);
        }
    }
}

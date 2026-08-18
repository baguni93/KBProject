package org.scoula.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.exception.CustomException;
import org.scoula.exception.ErrorCode;
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
public class NotificationServiceImpl implements NotificationService {

    private final NotificationMapper notificationMapper;
    private final SimpMessagingTemplate messagingTemplate;

    // 알림 정책 :
    // 같은 대상에게 같은 종류의 알림이 이미 존재하는 경우
    // 각 알림 정책에 따라 중복 여부를 판단한다.

    @Override
    public void createCommentNotification(
            int senderId,
            int receiverId,
            Integer targetId,
            int actorCount
    ) {

        // 자기 자신 제외
        if (receiverId == senderId) {
            return;
        }

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(Enum.NotificationType.COMMENT)
                .targetId(targetId)
                .actorCount(actorCount)
                .build();

        log.info("send user = {}", senderId);

        create(requestDTO);
    }

    @Override
    public void createFriendRequestNotification(
            int senderId,
            int receiverId,
            Integer targetId
    ) {

        // 읽지 않은 친구 요청이 있는지 확인
        boolean exists =
                notificationMapper.existsFriendRequestNotification(
                        senderId,
                        receiverId,
                        targetId
                );

        log.info("알림 여부 : {}", exists);

        if (!exists) {

            var requestDTO = NotificationRequestDTO.builder()
                    .receiverId(receiverId)
                    .senderId(senderId)
                    .notificationType(
                            Enum.NotificationType.FRIEND_REQUEST
                    )
                    .targetId(targetId)
                    .build();

            log.info("send user = {}", senderId);

            create(requestDTO);
        }
    }

    @Override
    public void createFriendAcceptNotification(
            int senderId,
            int receiverId,
            Integer targetId
    ) {

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(
                        Enum.NotificationType.FRIEND_ACCEPT
                )
                .targetId(targetId)
                .build();

        log.info("send user = {}", senderId);

        create(requestDTO);
    }

    @Override
    public void createSettlementNotification(
            int senderId,
            int receiverId,
            Integer targetId,
            Enum.NotificationType type
    ) {

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(type)
                .targetId(targetId)
                .build();

        log.info("send user = {}", senderId);

        create(requestDTO);
    }

    /**
     * 좋아요 그룹이 완성되었을 때 실제 알림 생성
     *
     * senderId   : 그룹 대표 사용자
     * receiverId : 피드 작성자
     * targetId   : feedId
     * actorCount : 그룹에 포함된 좋아요 사용자 수
     */
    @Override
    public void createLikeNotification(
            int senderId,
            int receiverId,
            Integer targetId,
            int actorCount
    ) {

        // 자기 자신 제외
        if (receiverId == senderId) {
            return;
        }

        var requestDTO = NotificationRequestDTO.builder()
                .receiverId(receiverId)
                .senderId(senderId)
                .notificationType(
                        Enum.NotificationType.LIKE
                )
                .targetId(targetId)
                .actorCount(actorCount)
                .build();

        create(requestDTO);
    }

    @Override
    public NotificationResponseDTO create(
            NotificationRequestDTO request
    ) {

        NotificationVO notificationVO = request.toVo();

        notificationMapper.create(notificationVO);

        NotificationResponseDTO responseDTO =
                get(notificationVO.getNotificationId());

        // 개인 알림은 /user/queue/notifications만 사용
        try {

            if (responseDTO != null) {

                messagingTemplate.convertAndSendToUser(
                        String.valueOf(request.getReceiverId()),
                        "/queue/notifications",
                        responseDTO
                );

                log.info(
                        "실시간 웹소켓 알림 전송 완료 - receiverId: {}, type: {}, actorCount: {}",
                        request.getReceiverId(),
                        request.getNotificationType(),
                        request.getActorCount()
                );
            }

        } catch (Exception wsErr) {

            log.warn(
                    "실시간 웹소켓 알림 전송 예외 (무시): {}",
                    wsErr.getMessage()
            );
        }

        return responseDTO;
    }

    @Override
    public NotificationResponseDTO get(int notificationId) {

        // 관리자용 메서드
        NotificationVO notificationVO =
                notificationMapper.get(notificationId);

        return NotificationResponseDTO.of(notificationVO);
    }

    @Override
    public List<NotificationResponseDTO> getList(int userId) {

        List<NotificationVO> list =
                notificationMapper.getList(userId);

        return list.stream()
                .map(NotificationResponseDTO::of)
                .toList();
    }

    @Override
    public void read(int notificationId) {

        validate(notificationId);

        notificationMapper.read(notificationId);
    }

    @Override
    public void readAll(int userId) {

        notificationMapper.readAll(userId);
    }

    private void validate(int targetId) {

        var vo = get(targetId);

        if (vo == null) {
            throw new CustomException(
                    ErrorCode.NOTIFICATION_NOT_FOUND
            );
        }
    }
}
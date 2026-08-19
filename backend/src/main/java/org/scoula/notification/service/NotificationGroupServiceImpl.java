package org.scoula.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.notification.domain.NotificationGroupActorVO;
import org.scoula.notification.domain.NotificationGroupVO;
import org.scoula.notification.mapper.NotificationGroupMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationGroupServiceImpl
        implements NotificationGroupService {

    private static final int MAX_ACTOR_COUNT = 1;

    private final NotificationGroupMapper notificationGroupMapper;
    private final NotificationService notificationService;

    @Transactional
    @Override
    public void addActor(
            int feedId,
            int senderId,
            int receiverId,
            Enum.NotificationType notificationType
    ) {

        // 자기 자신에게 알림 X
        if (senderId == receiverId) {
            return;
        }

        String type = notificationType.name();

        // =========================
        // 1. 대기 중 그룹 조회
        // =========================

        NotificationGroupVO group =
                notificationGroupMapper.findWaitingGroup(
                        feedId,
                        receiverId,
                        type
                );

        // =========================
        // 2. 없으면 새 그룹 생성
        // =========================

        if (group == null) {

            group = NotificationGroupVO.builder()
                    .feedId(feedId)
                    .receiverId(receiverId)
                    .notificationType(type)
                    .actorCount(0)
                    .status("WAITING")
                    .build();

            notificationGroupMapper.createGroup(group);
        }

        // =========================
        // 3. actor 추가
        // =========================

        int inserted =
                notificationGroupMapper.createActor(
                        NotificationGroupActorVO.builder()
                                .groupId(group.getGroupId())
                                .userId(senderId)
                                .build()
                );

        // 이미 이 그룹에 들어간 사용자
        if (inserted == 0) {
            return;
        }

        // =========================
        // 4. actor count 증가
        // =========================

        notificationGroupMapper.increaseActorCount(
                group.getGroupId()
        );

        int actorCount =
                notificationGroupMapper.getActorCount(
                        group.getGroupId()
                );

        log.info(
                "알림 그룹 actor 추가 - groupId: {}, type: {}, actorCount: {}",
                group.getGroupId(),
                type,
                actorCount
        );

        // =========================
        // 5. 3명 도달
        // =========================

        if (actorCount < MAX_ACTOR_COUNT) {
            return;
        }

        Integer firstActorId =
                notificationGroupMapper.getFirstActor(
                        group.getGroupId()
                );

        if (firstActorId == null) {
            return;
        }

        // =========================
        // 6. 실제 알림 생성
        // =========================

        if (notificationType == Enum.NotificationType.LIKE) {

            notificationService.createLikeNotification(
                    firstActorId,
                    receiverId,
                    feedId,
                    actorCount
            );

        } else if (
                notificationType ==
                        Enum.NotificationType.COMMENT
        ) {

            notificationService.createCommentNotification(
                    firstActorId,
                    receiverId,
                    feedId,
                    actorCount
            );
        }

        // =========================
        // 7. 그룹 완료
        // =========================

        notificationGroupMapper.completeGroup(
                group.getGroupId()
        );
    }

    @Transactional
    @Override
    public void removeActor(
            int feedId,
            int userId,
            int receiverId,
            Enum.NotificationType notificationType
    ) {

        String type = notificationType.name();

        NotificationGroupVO group =
                notificationGroupMapper.findWaitingGroup(
                        feedId,
                        receiverId,
                        type
                );

        if (group == null) {
            return;
        }

        // 실제 actor 삭제
        int deleted =
                notificationGroupMapper.deleteActor(
                        group.getGroupId(),
                        userId
                );

        if (deleted == 0) {
            return;
        }

        // actor count 감소
        notificationGroupMapper.decreaseActorCount(
                group.getGroupId()
        );

        int actorCount =
                notificationGroupMapper.getActorCount(
                        group.getGroupId()
                );

        // 아무도 남지 않았으면 그룹 자체 삭제
        if (actorCount == 0) {

            notificationGroupMapper.deleteGroup(
                    group.getGroupId()
            );

            log.info(
                    "빈 알림 그룹 삭제 - groupId: {}, type: {}",
                    group.getGroupId(),
                    type
            );
        }
    }
}
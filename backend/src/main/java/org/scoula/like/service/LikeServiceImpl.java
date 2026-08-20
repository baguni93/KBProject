package org.scoula.like.service;

import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.common.util.Enum;
import org.scoula.like.mapper.LikeMapper;
import org.scoula.notification.service.NotificationGroupService;
import org.scoula.redis.RedisKey;
import org.scoula.task.service.TaskEventService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Log4j2
public class LikeServiceImpl implements LikeService {

    private final LikeMapper likeMapper;
    private final NotificationGroupService notificationGroupService;
    private final StatefulRedisConnection<String, String> redisConnection;
    private final TaskEventService taskEventService;


    @Transactional
    @Override
    public boolean toggle(int feedId, int userId) {

        String likeUserKey = RedisKey.likeUser(feedId);

        boolean liked = isLiked(feedId, userId);

        // =================================================
        // 좋아요 취소
        // =================================================
        if (liked) {

            likeMapper.delete(feedId, userId);

            // 좋아요 수 감소
            try {
                decreaseLikeCount(feedId);
            } catch (Exception e) {
                log.error("Redis like count decrease fail", e);
            }

            // 실제 좋아요 사용자 캐시 제거
            try {
                redisConnection.sync()
                        .srem(
                                likeUserKey,
                                String.valueOf(userId)
                        );
            } catch (Exception e) {
                log.error("Redis like user remove fail", e);
            }

            // 대기 중인 좋아요 알림 그룹에서도 제거
            int receiverId = likeMapper.getFeedOwner(feedId);

            notificationGroupService.removeActor(
                    feedId,
                    userId,
                    receiverId,
                    Enum.NotificationType.LIKE
            );

            return false;
        }

        // =================================================
        // 좋아요 등록
        // =================================================

        likeMapper.create(feedId, userId);

        // 좋아요 수 증가
        try {
            increaseLikeCount(feedId);
        } catch (Exception e) {
            log.error("Redis like count update fail", e);
        }

        // 실제 좋아요 사용자 캐시 추가
        try {
            redisConnection.sync()
                    .sadd(
                            likeUserKey,
                            String.valueOf(userId)
                    );
        } catch (Exception e) {
            log.error("Redis like user update fail", e);
        }

        // 피드 작성자
        int receiverId = likeMapper.getFeedOwner(feedId);

        // =================================================
        // 좋아요 알림 그룹 처리
        // =================================================

        notificationGroupService.addActor(
                feedId,
                userId,
                receiverId,
                Enum.NotificationType.LIKE
        );

        return true;
    }

    // =================================================
    // 좋아요 수 증가
    // =================================================

    @Override
    public void increaseLikeCount(int feedId) {

        String key = RedisKey.likeCount(feedId);

        String count = redisConnection.sync()
                .get(key);

        if (count == null) {

            int dbCount =
                    likeMapper.getLikeCount(feedId);

            redisConnection.sync()
                    .set(
                            key,
                            String.valueOf(dbCount)
                    );

            return;
        }

        redisConnection.sync()
                .incr(key);
    }

    // =================================================
    // 좋아요 수 감소
    // =================================================

    @Override
    public void decreaseLikeCount(int feedId) {

        String key = RedisKey.likeCount(feedId);

        String count = redisConnection.sync()
                .get(key);

        if (count == null) {

            int dbCount =
                    likeMapper.getLikeCount(feedId);

            redisConnection.sync()
                    .set(
                            key,
                            String.valueOf(dbCount)
                    );

            return;
        }

        int currentCount = Integer.parseInt(count);

        // 0 이하라면 더 이상 감소시키지 않음
        if (currentCount <= 0) {
            redisConnection.sync().set(key, "0");
            return;
        }

        redisConnection.sync()
                .decr(key);
    }

    // =================================================
    // 좋아요 여부
    // =================================================

    @Override
    public boolean isLiked(int feedId, int userId) {

        String key = RedisKey.likeUser(feedId);

        try {

            boolean exists =
                    redisConnection.sync()
                            .sismember(
                                    key,
                                    String.valueOf(userId)
                            );

            if (exists) {
                return true;
            }

            boolean dbExist =
                    likeMapper.exists(
                            feedId,
                            userId
                    );

            // DB에는 좋아요가 있지만
            // Redis 캐시에 없다면 cache warm
            if (dbExist) {

                try {

                    redisConnection.sync()
                            .sadd(
                                    key,
                                    String.valueOf(userId)
                            );

                } catch (Exception e) {

                    log.error(
                            "Redis cache warm fail",
                            e
                    );
                }
            }

            return dbExist;

        } catch (Exception e) {

            log.error(
                    "Redis read fail",
                    e
            );

            return likeMapper.exists(
                    feedId,
                    userId
            );
        }
    }

    // =================================================
    // 좋아요 수 조회
    // =================================================

    @Override
    public int getLikeCount(int feedId) {

        String key = RedisKey.likeCount(feedId);

        try {

            String count =
                    redisConnection.sync()
                            .get(key);

            if (count != null) {
                return Integer.parseInt(count);
            }

        } catch (Exception e) {

            log.error(
                    "Redis read fail",
                    e
            );
        }

        int likeCount =
                likeMapper.getLikeCount(feedId);

        // cache warm
        try {

            redisConnection.sync()
                    .set(
                            key,
                            String.valueOf(likeCount)
                    );

        } catch (Exception e) {

            log.error(
                    "Redis cache save fail",
                    e
            );
        }

        return likeCount;
    }
}
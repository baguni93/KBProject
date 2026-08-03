package org.scoula.like.service;

import io.lettuce.core.api.StatefulRedisConnection;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.scoula.like.mapper.LikeMapper;
import org.scoula.notification.service.NotificationService;
import org.scoula.redis.RedisKey;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Log4j2
public class LikeServiceImpl implements  LikeService {
    private final LikeMapper likeMapper;
    private final NotificationService notificationService;
    private final StatefulRedisConnection<String, String> redisConnection;

    @Transactional
    @Override
    public boolean toggle(int feedId, int userId) {

        String key = RedisKey.likeUser(feedId);

        boolean liked = isLiked(feedId, userId);

        // 이미 좋아요를 눌렀는지 확인
        if (liked) {
            likeMapper.delete(feedId, userId);

            try{
                decreaseLikeCount(feedId);

            }catch(Exception e){
                log.error("Redis like count decrease fail",e);
            }


            try{
                redisConnection.sync()
                        .srem(
                                key,
                                String.valueOf(userId)
                        );

            }catch(Exception e){
                log.error("Redis like user remove fail",e);
            }
            return false;
        }

        likeMapper.create(feedId, userId);


        try{
            increaseLikeCount(feedId);
        }catch(Exception e){
            log.error("Redis like count update fail", e);
        }


        try{
            redisConnection.sync()
                    .sadd(
                            key,
                            String.valueOf(userId)
                    );
        }catch(Exception e){
            log.error("Redis like user update fail", e);
        }

        int receiverId = likeMapper.getFeedOwner(feedId);

        notificationService.createLikeNotification(userId, receiverId , feedId);

        return true;
    }
    @Override
    public void increaseLikeCount(int feedId){

        String key = RedisKey.likeCount(feedId);

        String count = redisConnection.sync()
                .get(key);

        if(count == null){

            int dbCount = likeMapper.getLikeCount(feedId);

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
    @Override
    public void decreaseLikeCount(int feedId){

        String key = RedisKey.likeCount(feedId);

        String count = redisConnection.sync()
                .get(key);


        if(count == null){

            int dbCount = likeMapper.getLikeCount(feedId);

            redisConnection.sync()
                    .set(
                            key,
                            String.valueOf(dbCount)
                    );
            return;
        }

        redisConnection.sync()
                .decr(key);
    }

    @Override
    public boolean isLiked(int feedId, int userId){

        String key = RedisKey.likeUser(feedId);

        try {

            boolean exists = redisConnection.sync()
                    .sismember(
                            key,
                            String.valueOf(userId)
                    );

            if(exists){
                return true;
            }

            boolean dbExist = likeMapper.exists(feedId,userId);

            if(dbExist){

                try {
                    redisConnection.sync()
                            .sadd(
                                    key,
                                    String.valueOf(userId)
                            );
                } catch(Exception e){
                    log.error("Redis cache warm fail", e);
                }

            }

            return dbExist;


        } catch(Exception e){

            log.error("Redis read fail", e);

            return likeMapper.exists(feedId,userId);
        }
    }

    @Override
    public int getLikeCount(int feedId){

        String key = RedisKey.likeCount(feedId);

        try {

            String count = redisConnection.sync()
                    .get(key);

            if(count != null){
                return Integer.parseInt(count);
            }

        } catch(Exception e){

            log.error("Redis read fail", e);
        }


        int likeCount = likeMapper.getLikeCount(feedId);


        try {

            redisConnection.sync()
                    .set(
                            key,
                            String.valueOf(likeCount)
                    );

        } catch(Exception e){

            log.error("Redis cache save fail", e);
        }


        return likeCount;
    }
}

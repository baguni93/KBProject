package org.scoula.redis;

import io.lettuce.core.api.StatefulRedisConnection;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RedisTestService {

    @Autowired
    private StatefulRedisConnection<String, String> redisConnection;


    public void test() {

        // 저장
        redisConnection.sync()
                .set("test-key", "hello redis");


        // 조회
        String value = redisConnection.sync()
                .get("test-key");


        System.out.println("Redis 결과 : " + value);
    }
}
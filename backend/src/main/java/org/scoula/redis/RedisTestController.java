package org.scoula.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RedisTestController {

    @Autowired
    private RedisTestService redisTestService;


    @GetMapping("/redis-test")
    public String test() {

        redisTestService.test();

        return "success";
    }
}
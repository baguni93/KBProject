package org.scoula.redis;

public class RedisKey {

    public static String likeCount(int feedId){
        return "feed:like:count:" + feedId;
    }

    public static String likeUser(int feedId){
        return "feed:like:user:" + feedId;
    }
}

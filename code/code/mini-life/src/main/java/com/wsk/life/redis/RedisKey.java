package com.wsk.life.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

/**
 * redis的Key定义
 * */
@Configuration
public class RedisKey {
    //redis域名
    @Value("${spring.redis.domain}")
    private String _redisDomain;

    public static String redisDomain;

    @PostConstruct
    public  void  init(){
        redisDomain = _redisDomain;
    }

    //登录token的Key
    public static String getLoginTokenKey(String key){
        return redisDomain + ":login:token:" + key;
    }
    //电影的Key
    public static String getMovieKey(String key){
        return redisDomain + ":movie:" + key;
    }
    //音乐的Key
    public static String getMusicKey(String key){
        return redisDomain + ":music:" + key;
    }
}

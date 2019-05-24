package com.chen.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * 生成 RedisPool,根据 RedisConfig 配置信息封装成 bean
 */

@Component
public class RedisPoolFactory {
    @Autowired
    RedisConfig redisConfig;

    @Bean
    public JedisPool jedisPoolFactory(){
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxIdle(redisConfig.getPoolMaxIdle());
        poolConfig.setMaxTotal(redisConfig.getPoolMaxTotal());
        poolConfig.setMaxWaitMillis(redisConfig.getPoolMaxWait());


        // timeout: 单位：毫秒
        JedisPool pool = new JedisPool(poolConfig,redisConfig.getHost(),redisConfig.getPort(),redisConfig.getTimeout()*1000,redisConfig.getPassword(),0);

        return pool;
    }
}

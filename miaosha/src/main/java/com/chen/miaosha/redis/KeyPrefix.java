package com.chen.miaosha.redis;

/**
 *  生成一个唯一的key前缀，防止key相同时，value被覆盖
 */
public interface KeyPrefix {
    /**
     * 获取过期时间
     */
    int expireSeconds();

    /**
     * 获得拼接后的前缀
     */
    String getPrefix();
}

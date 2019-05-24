package com.chen.miaosha.util;

import java.util.UUID;

/**
 *  生成唯一 UUID
 */
public class UUIDUtil {
    public static String uuid() {
        return UUID.randomUUID().toString().replace("-", "");
    }
}

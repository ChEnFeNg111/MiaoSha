package com.chen.miaosha.access;

import com.chen.miaosha.domain.MiaoShaUser;

/**
 *  防止高并发时出现线程安全问题，使用 ThreadLocal 来保存每个线程自己的数据，互不影响
 */
public class UserContext {

    private static ThreadLocal<MiaoShaUser> userThreadLocal = new ThreadLocal<>();

    public static void setUser(MiaoShaUser user){
        userThreadLocal.set(user);
    }

    public static MiaoShaUser getUser(){
        return  userThreadLocal.get();
    }
}

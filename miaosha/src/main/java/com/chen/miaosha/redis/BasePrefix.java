package com.chen.miaosha.redis;

/**
 *    定义一个抽象类，实现了KeyPrefix
 */
public abstract  class BasePrefix  implements KeyPrefix{

    // 过期时间
    private int expireSeconds;
    // 生成的前缀
    private String prefix;

    public BasePrefix(String prefix){
        this(0,prefix);
    }

    public BasePrefix(int expireSeconds,String prefix){
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    /**
     * @return  获取过期时间
     */
    @Override
    public int expireSeconds() {
        return expireSeconds;
    }

    /**
     * 利用 类名 拼接生成 唯一前缀
     */
    @Override
    public String getPrefix() {
        String className = getClass().getSimpleName();

        return className+":"+prefix;
    }
}

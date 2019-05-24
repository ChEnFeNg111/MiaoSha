package com.chen.miaosha.redis;

/**
 *  使用了 模板模式：
 *        定义一个接口 KeyPrefix ，利用一个抽象类 BasePrefix 实现它，最后再由具体的实现类 UserKey 继承进行实现
 */
public class UserKey extends BasePrefix {

    /**
     * 将构造函数私有，防止外部创建该类的实例
     */
    private UserKey(String prefix){
        super(prefix);
    }

    /**
     *  根据 id / name 来进行拼接唯一的 prefix
     */
    public static  UserKey getById = new UserKey("id");
    public static  UserKey getByName = new UserKey("name");
}

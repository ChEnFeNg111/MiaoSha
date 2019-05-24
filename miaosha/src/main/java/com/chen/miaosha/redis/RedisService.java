package com.chen.miaosha.redis;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.ScanParams;
import redis.clients.jedis.ScanResult;

import java.util.ArrayList;
import java.util.List;

/**
 * 利用 Jedis 操作 redis 缓存
 */
@Service
public class RedisService {

    // 从 JedisPool 池中取出 一个 redis
    @Autowired
    JedisPool pool;

    /**
     * 将 bean 转化为  String
     * @param value
     * @param <T>
     * @return
     */
    public static  <T> String beanToString(T value){
        if(value == null){
            return null;
        }

        Class<?> clazz = value.getClass();

        if(clazz == int.class || clazz == Integer.class){
            return ""+value;
        }else if(clazz == long.class || clazz == Long.class){
            return ""+value;
        }else if(clazz == String.class){
            return (String) value;
        }else {
            return JSON.toJSONString(value);
        }

    }


    /**
     *  将 String  转化为 bean
     * @param str     JSON字符串
     * @param clazz
     * @param <T>
     * @return
     */

    public static   <T> T stringToBean(String str,Class<T> clazz){
        if(str == null || str.length()<0 || clazz == null){
            return null;
        }

        if(clazz == int.class || clazz == Integer.class){
            return (T) Integer.valueOf(str);
        }else if(clazz == long.class || clazz == Long.class){
            return (T) Long.valueOf(str);
        }else if(clazz == String.class){
            return (T) str;
        }else {
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    /**
     *  根据生成的唯一 key 取查询 redis ,并将结果返回
     * @param prefix  前缀
     * @param key     键
     * @param clazz   类对象
     * @param <T>
     * @return        将 redis 取出的 字符串str 转换为 对象T 返回
     */
    public  <T> T  getKey(KeyPrefix prefix,String key, Class<T> clazz){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            //生成真正的 key ,根据这个key去redis缓存中寻找对应的值
            String realKey = prefix.getPrefix()+key;
            String str = jedis.get(realKey);
            T t = stringToBean(str, clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     *  先生成唯一的 key,再将该 key 和 转换后的 str 存入redis缓存中
     * @param prefix
     * @param key
     * @param value
     * @return
     */
    public <T> boolean  setKey(KeyPrefix prefix,String key, T value){
        Jedis jedis = null;
        try {
            jedis = pool.getResource();
            String str = beanToString(value);
            if(str == null || str.length()<0){
               return false;
            }

            //生成真正的 key ,根据这个key去redis缓存中寻找对应的值
            String realKey = prefix.getPrefix()+key;

            // 获取过期时间，如果过期了就直接存入键值对，否则继续使用这个未过期的时间
            int seconds = prefix.expireSeconds();
            if(seconds <= 0){
                jedis.set(realKey,str);
            }else {
                jedis.setex(realKey,seconds,str);
            }

            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 判断 key 是否存在
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> boolean isExists(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            //生成真正的key
            String realKey = prefix.getPrefix()+key;

            return  jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     * 删除指定的 key
     * @param prefix
     * @param key
     * @return
     */
    public boolean removeKey(KeyPrefix prefix,String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();

            String realKey = prefix.getPrefix()+key;

            Long del = jedis.del(realKey);

            return del > 0 ;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *  Redis Incr 将 key 中储存的数字值增一。
     *     如果 key 不存在，那么 key 的值会先被初始化为 0 ，然后再执行 INCR 操作。
     *     如果值包含错误的类型，或字符串类型的值不能表示为数字，那么返回一个错误。
     *     本操作的值限制在 64 位(bit)有符号数字表示之内
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long incr(KeyPrefix prefix,String key){
        Jedis jedis  = null;
        try{
            jedis = pool.getResource();

            String realKey = prefix.getPrefix()+key;

            return  jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     *  decr递减1并返回递减后的结果
     * @param prefix
     * @param key
     * @param <T>
     * @return
     */
    public <T> Long decr(KeyPrefix prefix,String key){
        Jedis jedis  = null;
        try{
            jedis = pool.getResource();

            String realKey = prefix.getPrefix()+key;

            return  jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *  删除多个指定的 key
     * @param prefix
     * @return
     */
    public boolean removeAllKey(KeyPrefix prefix){
        if(prefix == null){
            return false;
        }

        List<String> keys = scanKey(prefix.getPrefix());

        if(keys == null || keys.size() <0){
            return true;
        }

        Jedis jedis = null;
        try{
            jedis = pool.getResource();

            // 一次删除多个key
            jedis.del(keys.toArray(new String[0]));

            return true;
        }catch (final Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            returnToPool(jedis);
        }
    }

    /**
     *   命令格式：
     *     SCAN cursor [MATCH pattern] [COUNT count]
     *          命令解释：scan 游标 MATCH <返回和给定模式相匹配的元素> count 每次迭代所返回的元素数量
     *

     * SCAN命令是增量的循环，每次调用只会返回一小部分的元素。所以不会有KEYS命令的坑(key的数量比较多，一次KEYS查询会block其他操作)。  
     * SCAN命令返回的是一个游标，从0开始遍历，到0结束遍历。
     * 通过scan中的MATCH <pattern> 参数，可以让命令只返回和给定模式相匹配的元素，实现模糊查询的效果
     *
     * @param key
     * @return
     */
    public List<String>  scanKey(String key){
        Jedis jedis = null;
        try{
            jedis = pool.getResource();
            List<String> keys = new ArrayList<>();

            // 定义一个标记
            String cursor = "0";

            ScanParams  scanParams = new ScanParams();

            // 匹配的正则表达式
            scanParams.match("*"+key+"*");

            // 每次返回的数量
            scanParams.count(100);

            do{
                // cursor: 表示开始遍历的游标 , scanParams 是ScanParams 对象，此对象可以设置 每次返回的数量，以及遍历时的正则表达式
                ScanResult<String> res = jedis.scan(cursor,scanParams);

                //返回结果
                List<String> result = res.getResult();
                if(result != null && result.size()>0) {
                    keys.addAll(result);
                }

                // 返回用于下次遍历的游标
                cursor = res.getStringCursor();
            }while (!cursor.equals("0"));

            return keys;

        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *  将Jedis 返回给 JedisPool
     * @param jedis
     */
    public  void returnToPool(Jedis jedis){
        if(jedis != null){
            jedis.close();
        }
    }


}

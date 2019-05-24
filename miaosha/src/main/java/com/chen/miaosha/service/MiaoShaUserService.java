package com.chen.miaosha.service;

import com.chen.miaosha.dao.MiaoShaUserDao;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.exception.GlobalException;
import com.chen.miaosha.redis.MiaoshaUserKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.util.MD5Util;
import com.chen.miaosha.util.UUIDUtil;
import com.chen.miaosha.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

@Service
public class MiaoShaUserService{


    // 键值对 token:sessionid
    public static String COOKIE_NAME_TOKEN = "token";

    @Autowired
    MiaoShaUserDao miaoShaUserDao;

    @Autowired
    RedisService redisService;


    /**
     * 根据id 进行查找，先到缓存中查找是否存在该数据，存在则直接返回
     * 否则进入到数据库中查找，查找后先放入缓存中，在进行返回
     * @param id
     * @return
     */
    public MiaoShaUser getByUserId(long id){

        // 取缓存
        MiaoShaUser user = redisService.getKey(MiaoshaUserKey.getById, "" + id, MiaoShaUser.class);

        if(user != null){
            return user;
        }

        //若缓存redis中没有数据，则到数据库中查询
        user =  miaoShaUserDao.getById(id);

        if(user != null){
            redisService.setKey(MiaoshaUserKey.getById,""+id,user);
        }

        return user;
    }


    /**
     *  进行登录操作，并对数据进行验证
     * @param response
     * @param loginVo
     * @return
     */
    public String login(HttpServletResponse response, LoginVo loginVo){
        if(loginVo == null ){
            throw new GlobalException(CodeMsg.SERVER_ERROR);
        }

        // 表单提交的 手机号 和 密码
        String mobile = loginVo.getMobile();
        String formPass = loginVo.getPassword();

        // 判断手机号是否存在
        MiaoShaUser user = getByUserId(Long.parseLong(mobile));

        // 手机号不存在
        if(user == null){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 验证数据库中的 密码 和 盐值
        String dbPass = user.getPassword();
        String dbSalt = user.getSalt();

        // 验证密码是否正确
        String calPass = MD5Util.formPassToDBPass(formPass, dbSalt);
        if(!calPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.PASSWORD_ERROR);
        }

        // 生成Cookie
        String token = UUIDUtil.uuid();

        createCookie(response,token,user);

        return token;
    }

    /**
     *   根据 请求中 Cookie 的token,在缓存 redis 中查找
     * @param response
     * @param token
     * @return
     */
    public MiaoShaUser getByToken(HttpServletResponse response,String token){

        if(StringUtils.isEmpty(token)){
            return null;
        }

        // 根据 请求中 Cookie 的token,在缓存 redis 中查找
        MiaoShaUser user = redisService.getKey(MiaoshaUserKey.token, token, MiaoShaUser.class);

        // 延长有效期：更新至最后一次访问的时间
        if(user != null){
            createCookie(response,token,user);
        }

        return user;

    }


    /**
     *  更新对象级缓存：
     *      先更新数据库，再同步缓存 （https://blog.csdn.net/tTU1EvLDeLFq5btqiK/article/details/78693323）
     * @param token
     * @param id
     * @param newPassword
     * @return
     */
    public boolean updatePassword(String token,long id,String newPassword){

        // 取 User
        MiaoShaUser user = getByUserId(id);

        if(user == null ){
            throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
        }

        // 更新数据库
        String newPass = MD5Util.formPassToDBPass(newPassword, user.getSalt());

        // 新建一个对象是为了执行 SQL 语句时，只更新密码，其他字段不更新，为了减少 bin_log 的产生
        MiaoShaUser newUser = new  MiaoShaUser();
        newUser.setId(id);
        newUser.setPassword(newPass);

        miaoShaUserDao.update(newUser);

        // 更新缓存
        redisService.removeKey(MiaoshaUserKey.getById,""+id);
        user.setPassword(newUser.getPassword());
        redisService.setKey(MiaoshaUserKey.token,token,newUser);

        return true;

    }

    /**
     * 生成 Cookie : token = sessionid
     *
     *
     * @param response
     * @param token
     * @param user
     */
    private void  createCookie(HttpServletResponse response,String token,MiaoShaUser user){
        // 分布式session: 将 生成的 session（token） 存入redis缓存中，
        redisService.setKey(MiaoshaUserKey.token,token,user);

        Cookie cookie  = new Cookie(COOKIE_NAME_TOKEN,token);

        // 将Cookie 的过期时间与 Session 的过期时间设置成一致的，保证一致性
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());

        cookie.setPath("/");

        // 将Cookie 添加进 Response 响应中，第二次访问时会将该token放到 request 请求中的 Cookie
        response.addCookie(cookie);

    }

}

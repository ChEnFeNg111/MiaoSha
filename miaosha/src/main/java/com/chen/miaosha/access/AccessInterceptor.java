package com.chen.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.redis.AccessKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.MiaoShaUserService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;


import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;


/**
 *  拦截器：限制访问次数，利用自定义的 @AccessLimit 注解进行实现
 */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    private static Logger logger = LoggerFactory.getLogger(AccessInterceptor.class);

    @Autowired
    MiaoShaUserService userService;

    @Autowired
    RedisService redisService;

    /**
     *  拦截后，进行对 访问次数 的判断
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if(handler instanceof HandlerMethod){
            MiaoShaUser user = getUser(request, response);

            //放入当前线程自己的内存中 ThreadLocal
            UserContext.setUser(user);

            HandlerMethod handlerMethod = (HandlerMethod) handler;
            AccessLimit limit = handlerMethod.getMethodAnnotation(AccessLimit.class);

            // 如果该方法没有加 @AccessLimit 注解，则不做任何操作
            if(limit == null){
                return true;
            }

            int seconds = limit.seconds();
            int maxCount = limit.maxCount();
            boolean needLogin = limit.needLogin();
            String key = request.getRequestURI();


            // 判断该用户有没有登录
            if(needLogin){
                if(user == null){
                    render(response,CodeMsg.SESSION_ERROR);
                    return false;
                }
                key +="_"+user.getId();
            }

            // 设置限制访问的时间，并判断是否到达限制访问的次数
            AccessKey accessKey = AccessKey.withExpire(seconds);
            Integer count = redisService.getKey(accessKey, key, Integer.class);
            if(count == null){
                redisService.setKey(accessKey,key,1);
            }else if(count < maxCount){
                redisService.incr(accessKey,key);
            }else {
                render(response,CodeMsg.ACCESS_LIMIT_REACHED);
                return false;
            }
        }

        return true;
    }

    /**
     *  利用 OutputStream 向页面输出提示信息
     * @param response
     * @param msg
     * @throws IOException
     */
    private void render(HttpServletResponse response, CodeMsg msg) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        OutputStream outputStream = response.getOutputStream();

        // 将 Result.error(msg) 转化为 JSON数据
        String json = JSON.toJSONString(Result.error(msg));

        // 输出到页面中显示
        outputStream.write(json.getBytes("UTF-8"));
        outputStream.flush();
        outputStream.close();

    }

    /**
     *  处理请求参数，验证该用户是否已经登录过了
     * @param request
     * @param response
     * @return
     */
    private MiaoShaUser getUser(HttpServletRequest request, HttpServletResponse response) {

        String paramToken = request.getParameter(MiaoShaUserService.COOKIE_NAME_TOKEN);
        String cookieToken = getCookieValue(request, MiaoShaUserService.COOKIE_NAME_TOKEN);

        if(StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken)) {
            return null;
        }
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return userService.getByToken(response, token);
    }

    /**
     *   遍历cookie,找到指定的 token 的值
     * @param request
     * @param cookiName
     * @return
     */
    private String getCookieValue(HttpServletRequest request, String cookiName) {
        Cookie[]  cookies = request.getCookies();
        if(cookies == null || cookies.length <= 0){
            return null;
        }
        for(Cookie cookie : cookies) {
            if(cookie.getName().equals(cookiName)) {
                return cookie.getValue();
            }
        }
        return null;
    }
}

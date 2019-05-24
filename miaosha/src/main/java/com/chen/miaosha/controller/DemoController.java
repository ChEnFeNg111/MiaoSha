package com.chen.miaosha.controller;

import com.chen.miaosha.domain.User;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.redis.UserKey;
import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/demo")
public class DemoController {
    @Autowired
    UserService userService;

    @Autowired
    RedisService redisService;

    @RequestMapping("/thymeleaf")
    public String demo(Model model){
        System.out.println("hello");
        model.addAttribute("name","chen");
        return "hello";
    }

    /**
     * 测试返回JSON数据
     * @return
     */
    @RequestMapping("/success")
    @ResponseBody
    public Result<String> successDemo(){
        return Result.success("hello,success");
    }


    @RequestMapping("/error")
    @ResponseBody
    public Result<String> errorDemo(){
        return Result.error(CodeMsg.SERVER_ERROR);
    }

    /**
     * 测试链接数据库
     */
    @RequestMapping("/get")
    @ResponseBody
    public Result<User> getById(){
        User user = userService.getById(1);
        return Result.success(user);
    }

    /**
     * 测试数据库事务
     */
    @RequestMapping("/tx")
    @ResponseBody
    public Result<Boolean> tx(){
        userService.tx();
        return Result.success(true);
    }

    /**
     * 测试 redis
     */
    @RequestMapping("/redisGet")
    @ResponseBody
    public Result<User> get(){
        User user = redisService.getKey(UserKey.getById, "" + 1, User.class);
        return  Result.success(user);
    }


    @RequestMapping("/redisSet")
    @ResponseBody
    public Result<Boolean> set(){
        User user = new User();
        user.setId(1);
        user.setName("chen");
        redisService.setKey(UserKey.getById, "" + 1, user);
        return  Result.success(true);
    }




}

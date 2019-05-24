package com.chen.miaosha.controller;

import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.MiaoShaUserService;
import com.chen.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;


@Controller
@RequestMapping("/login")
public class LoginController {


    // 打印日志信息
    private static Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    MiaoShaUserService miaoShaUserService;

    /**
     *  登录入口
     * @return
     */
    @RequestMapping("/toLogin")
    public String toLogin(){
        return "login";
    }

    /**
     *  执行登录操作
     * @param response
     * @param loginVo
     * @return
     */
    @RequestMapping("/doLogin")
    @ResponseBody
    public Result<String> doLogin(HttpServletResponse response, @Valid LoginVo loginVo){

        // 执行登录操作
        String token = miaoShaUserService.login(response,loginVo);

        // 登录成功返回
        return Result.success(token);
    }




}

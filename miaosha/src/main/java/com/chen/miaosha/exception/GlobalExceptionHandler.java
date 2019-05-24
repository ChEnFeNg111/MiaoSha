package com.chen.miaosha.exception;

import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.result.Result;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.validation.BindException;

import javax.servlet.http.HttpServletRequest;
import java.util.List;


/**
 *  全局异常处理器:只要 Controller 中抛出异常，就到这来处理
 */
// 切面
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler{

    @ExceptionHandler(value = Exception.class)
    public Result<String>  exceptionHandler(HttpServletRequest request, Exception e){
        e.printStackTrace();

        if(e instanceof GlobalException){
            GlobalException ex  = (GlobalException) e;
            return Result.error(ex.getMsg());
        }else if(e instanceof BindException ){
            BindException ex = (BindException) e;
            //异常可能有多种类型
            List<ObjectError> allErrors = ex.getAllErrors();
            ObjectError exception = allErrors.get(0);
            String defaultMessage = exception.getDefaultMessage();
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(defaultMessage));
        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}

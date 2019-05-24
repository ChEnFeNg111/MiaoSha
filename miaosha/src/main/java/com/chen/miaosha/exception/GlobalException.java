package com.chen.miaosha.exception;

import com.chen.miaosha.result.CodeMsg;

/**
 *  自定义异常： 全局异常
 */

public class GlobalException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private CodeMsg msg;

    public GlobalException(CodeMsg msg){
        super(msg.toString());
        this.msg = msg;
    }

    public CodeMsg getMsg() {
        return msg;
    }
}

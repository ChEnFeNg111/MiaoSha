package com.chen.miaosha.result;

/**
 * 生成 Rest Api 格式的 JSON、
 * 例：
 *  {
 *      "code": 0,          //信息编码
 *      "msg": "success",   //提示信息
 *      "data":"hello"      //详细数据
 *  }
 *
 * @param <T>
 */
public class Result<T> {

    private int code;
    private String msg;
    private T data;

    private Result(T data){
        this.data = data;
    }

    private Result(int code,String msg){
        this.code = code;
        this.msg = msg;
    }

    private Result(CodeMsg codeMsg){
        if(codeMsg != null){
            this.code = codeMsg.getCode();
            this.msg = codeMsg.getMsg();
        }

    }

    /**
     * 成功时调用
     */
    public static  <T> Result<T> success(T data){
        return new Result<T>(data);
    }

    /**
     * 失败时调用
     */
    public static  <T> Result<T> error(CodeMsg codeMsg){
        return new Result<T>(codeMsg);
    }


    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}

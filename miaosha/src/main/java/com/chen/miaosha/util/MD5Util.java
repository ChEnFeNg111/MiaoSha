package com.chen.miaosha.util;


import org.apache.commons.codec.digest.DigestUtils;


public class MD5Util {

    // 固定的盐值
    private static final String SALT = "1a2b3c4d";

    /**
     *  进行 MD5 加密
     * @param src
     * @return
     */
    public static  String md5(String src){
        return DigestUtils.md5Hex(src);
    }

    /**
     *  将 表单输入的密码 进行第一次加密
     * @param inputPass
     * @return
     */
    public static String inputPassToFormPass(String inputPass){
        String str = ""+SALT.charAt(0)+SALT.charAt(2) + inputPass +SALT.charAt(5) + SALT.charAt(4);
        return md5(str);
    }

    /**
     *  根据随机的盐值进行第二次加密，将 加密后的数据 和 盐值 存入数据库中
     * @param formPass
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String formPass, String salt) {
        String str = ""+salt.charAt(0)+salt.charAt(2) + formPass +salt.charAt(5) + salt.charAt(4);
        return md5(str);
    }

    /**
     *  得到第二次加密后的密码
     * @param inputPass
     * @param saltDB
     * @return
     */
    public static String inputPassToDbPass(String inputPass, String saltDB) {
        String formPass = inputPassToFormPass(inputPass);
        String dbPass = formPassToDBPass(formPass, saltDB);
        return dbPass;
    }

}

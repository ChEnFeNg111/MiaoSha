package com.chen.miaosha.service;

import com.chen.miaosha.domain.MiaoShaOrder;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.domain.OrderInfo;
import com.chen.miaosha.redis.MiaoshaKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.util.MD5Util;
import com.chen.miaosha.util.UUIDUtil;
import com.chen.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

@Service
public class MiaoShaService {

    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;


    /**
     * 减库存 下订单 写入秒杀订单  是一个整体的事务
     */
    @Transactional
    public OrderInfo miaoSha(MiaoShaUser user, GoodsVo goodsVo){

        // 减库存
        boolean success = goodsService.reduceStock(goodsVo);

        // 减库存成功，才会下订单
        if(success){
            return  orderService.createOrder(user, goodsVo);
        }else {
            // 商品被秒杀完毕
            setGoodsOver(goodsVo.getId());
            return null;
        }
    }


    /**
     *  获取秒杀结果：
     *    result = orderId:  秒杀成功
     *    result = 0:        排队中
     *    result = -1:       秒杀失败
     * @param id
     * @param goodsId
     * @return
     */
    public long getMiaoShaResult(Long id, long goodsId) {

        MiaoShaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(id, goodsId);

        // 秒杀成功
        if(order != null){
            return order.getOrderId();
        }else {
            boolean isOver = getGoodsOver(goodsId);
            //秒杀完毕
            if(isOver){
                return -1;
            }else {//排队中
                return 0;
            }
        }
    }

    /**
     *  判断指定商品的库存是否还有剩余，还有剩余就正处于排队中
     *
     *    false: 剩余
     *    true:  秒杀完毕
     *
     * @param goodsId
     * @return
     */
    private boolean getGoodsOver(long goodsId) {
        return redisService.isExists(MiaoshaKey.isGoodsOver, "" + goodsId);
    }

    /**
     *  如果指定的商品库存被秒杀完毕了，就添加一个键值对 商品id：true
     * @param goodsId
     */
    private void setGoodsOver(Long goodsId) {
        redisService.setKey(MiaoshaKey.isGoodsOver,""+goodsId,true);
    }


    /**
     *  重置库存数据
     * @param list
     */
    public void reset(List<GoodsVo> list) {
        goodsService.resetStock(list);
        orderService.deleteOrders();
    }

    /**
     *  对秒杀路径进行检验
     * @param user
     * @param path
     * @param goodsId
     * @return
     */
    public boolean checkPath(MiaoShaUser user, String path, long goodsId) {
        if(user == null || path == null){
           return false;
        }

        String redisPath = redisService.getKey(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId, String.class);

        return path.equals(redisPath);
    }


    /**
     *  随机生成秒杀路径 path
     * @param user
     * @param goodsId
     * @return
     */
    public String createMiaoShaPath(MiaoShaUser user,long goodsId) {
        if(user == null || goodsId<=0){
            return null;
        }

        // 生成随机路径path
        String path = MD5Util.md5(UUIDUtil.uuid() + "12345");

        // 存入 redis
        redisService.setKey(MiaoshaKey.getMiaoshaPath,""+user.getId()+"_"+goodsId,path);

        return path;
    }


    /**
     *  对图形验证码进行验证
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    public boolean checkVerifyCode(MiaoShaUser user, long goodsId, int verifyCode) {
        if(user == null || goodsId <0){
            return false;
        }

        Integer redisVerifyCode = redisService.getKey(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+"_"+goodsId, Integer.class);

        if(redisVerifyCode == null || redisVerifyCode-verifyCode != 0){
            return false;
        }

        // 验证完后需要将该验证码从redis中删除，因为不需要再使用了
        redisService.removeKey(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+"_"+goodsId);

        return true;
    }


    /**
     *  生成验证码的图片
     * @param user
     * @param goodsId
     * @return
     */
    public BufferedImage createVerifyCode(MiaoShaUser user, long goodsId) {
        if(user == null || goodsId <=0) {
            return null;
        }
        int width = 80;
        int height = 32;

        // 生成图片
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();

        // 设置背景图片
        g.setColor(new Color(0xDCDCDC));
        g.fillRect(0, 0, width, height);

        // 设置边界
        g.setColor(Color.black);
        g.drawRect(0, 0, width - 1, height - 1);

        // 设置干扰点
        Random rdm = new Random();
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);
        }
        // 将生成的计算表达式添加到图片上
        String exp = generateVerifyCode(rdm);
        g.setColor(new Color(0, 100, 0));
        g.setFont(new Font("Candara", Font.BOLD, 24));
        g.drawString(exp, 8, 24);
        g.dispose();

        //把验证码存到redis中
        int codeResult = calc(exp);
        redisService.setKey(MiaoshaKey.getMiaoshaVerifyCode,user.getId()+"_"+goodsId,codeResult);

        //输出图片
        return image;
    }

    /**
     *  利用 ScriptEngine 来计算图片中表达式的结果
     * @param exp
     * @return
     */
    private int calc(String exp) {
        ScriptEngineManager manager = new ScriptEngineManager();
        ScriptEngine engine = manager.getEngineByName("JavaScript");
        try {
            return (int) engine.eval(exp);

        } catch (ScriptException e) {
            e.printStackTrace();
            return 0;
        }
    }


    private static char[] ops = new char[] {'+', '-', '*'};
    /**
     * 生成表达式
     * + - *
     * */
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);
        char op1 = ops[rdm.nextInt(3)];
        char op2 = ops[rdm.nextInt(3)];
        String exp = ""+ num1 + op1 + num2 + op2 + num3;
        return exp;
    }
}

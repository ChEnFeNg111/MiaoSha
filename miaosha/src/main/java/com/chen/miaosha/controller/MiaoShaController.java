package com.chen.miaosha.controller;

import com.chen.miaosha.access.AccessLimit;
import com.chen.miaosha.domain.MiaoShaOrder;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.domain.OrderInfo;
import com.chen.miaosha.rabbitmq.MiaoshaMessage;
import com.chen.miaosha.rabbitmq.RabbitMQSender;
import com.chen.miaosha.redis.GoodsKey;
import com.chen.miaosha.redis.MiaoshaKey;
import com.chen.miaosha.redis.OrderKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.GoodsService;
import com.chen.miaosha.service.MiaoShaService;
import com.chen.miaosha.service.OrderService;
import com.chen.miaosha.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.classfile.Code;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;

@Controller
@RequestMapping("/miaosha")
public class MiaoShaController implements InitializingBean {

    private static Logger logger = LoggerFactory.getLogger(MiaoShaController.class);

    @Autowired
    MiaoShaService miaoShaService;

    //在别的Controller 中使用时，注入 XXXService,不要注入 XXXDao
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    RedisService redisService;

    @Autowired
    RabbitMQSender rabbitMQSender;

    /**
     * 存储： 商品id : false/true ， true: 秒杀完毕  false: 还有库存
     * 作用：可以根据库存判断该商品是否已经秒杀完毕，减少对redis的访问
     */
    private HashMap<Long,Boolean> localOverMap = new HashMap<>();

    /**
     *  系统初始化
     * @throws Exception
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> list = goodsService.listGoodsVo();
        if(list == null){
            return;
        }

        // 将 商品id:商品的库存 先存入redis中
        for (GoodsVo vo : list) {
            redisService.setKey(GoodsKey.getMiaoshaGoodsStock,""+vo.getId(),vo.getStockCount());
            localOverMap.put(vo.getId(),false);
        }
    }

    /**
     *  重置库存数据:   重置 MiaoShaOrder 和  OrderInfo 的信息（redis ,mysql）
     * @return
     */
    @RequestMapping(value = "/reset",method = RequestMethod.GET)
    public Result<Boolean> reset(){
        List<GoodsVo> list = goodsService.listGoodsVo();
        for (GoodsVo vo : list) {
            vo.setStockCount(10);
            redisService.setKey(GoodsKey.getMiaoshaGoodsStock,""+vo.getId(),10);
            localOverMap.put(vo.getId(),false);
        }

        // 重置缓存数据
        redisService.removeAllKey(OrderKey.getMiaoshaOrderByUidGid);
        redisService.removeAllKey(MiaoshaKey.isGoodsOver);

        // 重置数据库数据
        miaoShaService.reset(list);

        return Result.success(true);

    }

    /**
     *  优化:
     *      执行秒杀操作： 利用 MQ ，进行异步下单
     *      隐藏秒杀地址：/{path}/do_miaosha,增加安全性
     *
     * @param model
     * @param user
     * @param goodsId  需要秒杀的商品ID
     * @return
     */
    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)
    @ResponseBody
    public Result<Integer> doMiaoSha(Model model, MiaoShaUser user, @RequestParam("goodsId")long goodsId,@PathVariable("path")String path){
        model.addAttribute("user",user);

        //先判断是否已经登录了
        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 验证秒杀path正确,
        boolean check  =  miaoShaService.checkPath(user,path,goodsId);
        if(!check){
            //请求非法
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }


        // 在本地内存（localOverMap ）中判断，是否还有库存，减少对 redis 的访问
        Boolean isOver = localOverMap.get(goodsId);

        if(isOver){
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        // 预减库存：先在 redis 中把 库存-1
        Long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, "" + goodsId);

        if(stock < 0){
            // 商品已经秒杀完毕
            localOverMap.put(goodsId,true);
            return Result.error(CodeMsg.MIAO_SHA_OVER);
        }

        //从缓存中查找, 判断是否已经秒杀到了,避免同一个用户重复秒杀
        MiaoShaOrder miaoShaOrder =  redisService.getKey(OrderKey.getMiaoshaOrderByUidGid, "" + user.getId() + "_" + goodsId, MiaoShaOrder.class);

        if(miaoShaOrder != null){
            return Result.error(CodeMsg.REPEATE_MIAOSHA);
        }

        // 先入队（MQ），而不是直接到数据库中下订单
        MiaoshaMessage miaoshaMessage = new MiaoshaMessage();
        miaoshaMessage.setUser(user);
        miaoshaMessage.setGoodsId(goodsId);

        rabbitMQSender.sender(miaoshaMessage);


        // 0: 排队中
        return Result.success(0);
    }

    /**
     * 秒杀结果：
     *  result = orderId: 秒杀成功
     *  result = 0:       排队中
     *  result = -1:      秒杀失败
     *
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> miaoShaResult(MiaoShaUser user,@RequestParam("goodsId")long goodsId){

        if(user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        Long result = miaoShaService.getMiaoShaResult(user.getId(),goodsId);

        return Result.success(result);
    }


    /**
     *  随机生成秒杀路径，将真正的路径隐藏起来,防止恶刷
     * @param user
     * @param goodsId
     * @param verifyCode
     * @return
     */
    @AccessLimit(seconds = 5,maxCount = 5,needLogin = true)
    @RequestMapping(value = "/path",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoShaPath(MiaoShaUser user,
                                         @RequestParam("goodsId")long goodsId,
                                         @RequestParam(value="verifyCode", defaultValue="0")int verifyCode){
        if(user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }

        // 校验验证码是否正确
        boolean check = miaoShaService.checkVerifyCode(user,goodsId,verifyCode);

        if(!check){
            return Result.error(CodeMsg.REQUEST_ILLEGAL);
        }

        // 生成随机的路径 path
        String path = miaoShaService.createMiaoShaPath(user,goodsId);

        return Result.success(path);
    }

    @RequestMapping(value = "/verifyCode",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getMiaoShaVerifyCode(HttpServletResponse response, MiaoShaUser user, @RequestParam("goodsId")long goodsId){
        if(user == null ){
            return Result.error(CodeMsg.SESSION_ERROR);
        }


        try(OutputStream out = response.getOutputStream()){

            BufferedImage image = miaoShaService.createVerifyCode(user, goodsId);
            ImageIO.write(image,"JPEG",out);
            out.flush();

            return null;
        } catch (IOException e) {
            e.printStackTrace();
            return Result.error(CodeMsg.MIAOSHA_FAIL);
        }
    }
}

package com.chen.miaosha.rabbitmq;

import com.chen.miaosha.domain.MiaoShaOrder;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.service.GoodsService;
import com.chen.miaosha.service.MiaoShaService;
import com.chen.miaosha.service.OrderService;
import com.chen.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class RabbitMQReceiver {

    private static Logger log = LoggerFactory.getLogger(RabbitMQReceiver.class);

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoShaService miaoShaService;

    /**
     *  利用消息队列接收发过来的 秒杀信息（MiaoshaMessage）,进行异步下单
     * @param msg
     */
    @RabbitListener(queues = RabbitMQConfig.MIAOSHA_QUEUE)
    public void receiver(String msg){
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(msg,MiaoshaMessage.class);

        // MiaoShaMessage 秒杀信息：用户 和 商品编号
        MiaoShaUser user = miaoshaMessage.getUser();
        long goodsId = miaoshaMessage.getGoodsId();

        //访问数据库，判断数据库中的库存是否足够
        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
        if(goods.getStockCount() <= 0){
            return;
        }

        // 判断该用户是否已经秒杀过该商品了，避免重复秒杀
        MiaoShaOrder order = orderService.getMiaoshaOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null){
            return;
        }

        //减库存，下订单 ，写入秒杀订单
        miaoShaService.miaoSha(user, goods);


    }


    /**
     * 接收指定 routingkey (RabbitMQConfig.DIRECT_QUEUE) 的消息
     * @param msg
     *//*
    @RabbitListener(queues = RabbitMQConfig.DIRECT_QUEUE)
    public void directReciver(String msg){
        log.info("DirectReceiverMassage: "+msg);
    }

    *//**
     * 接收指定 routingkey (RabbitMQConfig.TOPIC_QUEUE1) 的消息
     * @param msg
     *//*
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE1)
    public void topticReciver1(String msg){
        log.info("TopticReceiver1Massage: "+msg);
    }

    *//**
     *  接收指定 routingkey (RabbitMQConfig.TOPIC_QUEUE2) 的消息
     * @param msg
     *//*
    @RabbitListener(queues = RabbitMQConfig.TOPIC_QUEUE2)
    public void topticReciver2(String msg){
        log.info("TopticReceiver2Massage: "+msg);
    }

    *//**
     * 接收指定 routingkey (RabbitMQConfig.TOPIC_QUEUE2) 的消息
     * @param msg
     *//*
    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE1)
    public void fanoutReciver1(String msg){
        log.info("FanoutReceiver1Massage: "+msg);
    }

    @RabbitListener(queues = RabbitMQConfig.FANOUT_QUEUE2)
    public void fanoutReciver2(String msg){
        log.info("FanoutReceiver2Massage: "+msg);
    }

    *//**
     *  接收指定 routingkey(RabbitMQConfig.HEADER_QUEUE) 的消息
     *//*
    @RabbitListener(queues = RabbitMQConfig.HEADER_QUEUE)
    public void  headersReceiver(String msg){
        log.info("HeadersReceiverMessage: "+msg);
    }*/
}

package com.chen.miaosha.service;


import com.chen.miaosha.dao.OrderDao;
import com.chen.miaosha.domain.MiaoShaOrder;
import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.domain.OrderInfo;
import com.chen.miaosha.redis.OrderKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;


@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;


    @Autowired
    RedisService redisService;

    /**
     *  改进： 从缓存中查找已经下过的订单
     * @param id
     * @param goodsId
     * @return
     */
    public MiaoShaOrder getMiaoshaOrderByUserIdGoodsId(Long id, long goodsId) {

        // 改进：先从缓存中查找已经下过的订单
        MiaoShaOrder order = redisService.getKey(OrderKey.getMiaoshaOrderByUidGid, "" + id + "_" + goodsId, MiaoShaOrder.class);

        // 缓存中没有再去数据库中查找
        if(order == null ){
            return orderDao.getMiaoshaOrderByUserIdGoodsId(id,goodsId);
        }else {
            return order;
        }
    }

    /**
     *  下订单 写入秒杀订单  是一个整体的事务
     *  订单完成时，将订单（MiaoShaOrder） 写入缓存 redis 中
     */

    @Transactional
    public OrderInfo createOrder(MiaoShaUser user ,GoodsVo goodsVo ){

        OrderInfo orderInfo = new OrderInfo();

       orderInfo.setCreateDate(new Date());
       orderInfo.setDeliveryAddrId(0L);
       orderInfo.setGoodsCount(1);
       orderInfo.setGoodsId(goodsVo.getId());
       orderInfo.setGoodsName(goodsVo.getGoodsName());
       orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
       orderInfo.setOrderChannel(1);
       orderInfo.setStatus(0);//'订单状态：0新建未支付，1待发货，2已发货，3已收货，4已退款，5已完成',
       orderInfo.setUserId(user.getId());

       // 插入之后，mybatis 会自动将数据赋值给该对象
       orderDao.insert(orderInfo);


        MiaoShaOrder miaoShaOrder = new MiaoShaOrder();

        miaoShaOrder.setGoodsId(goodsVo.getId());
        // 直接通过对象获取 orderId
        miaoShaOrder.setOrderId(orderInfo.getId());
        miaoShaOrder.setUserId(user.getId());

        // 写入秒杀订单
        orderDao.insertMiaoshaOrder(miaoShaOrder);

        // 写入缓存中
        redisService.setKey(OrderKey.getMiaoshaOrderByUidGid,""+user.getId()+"_"+goodsVo.getId(),miaoShaOrder);

        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }

    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteMiaoshaOrders();
    }

}

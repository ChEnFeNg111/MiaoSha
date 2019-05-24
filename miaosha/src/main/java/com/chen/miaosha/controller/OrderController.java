package com.chen.miaosha.controller;


import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.domain.OrderInfo;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.result.CodeMsg;
import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.GoodsService;
import com.chen.miaosha.service.MiaoShaUserService;
import com.chen.miaosha.service.OrderService;
import com.chen.miaosha.vo.GoodsVo;
import com.chen.miaosha.vo.OrderDetailVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;


@Controller
@RequestMapping("/order")
public class OrderController {

	@Autowired
	MiaoShaUserService userService;
	
	@Autowired
	RedisService redisService;
	
	@Autowired
	OrderService orderService;
	
	@Autowired
	GoodsService goodsService;
	
    @RequestMapping("/detail")
    @ResponseBody
    public Result<OrderDetailVo> info(Model model, MiaoShaUser user,
									  @RequestParam("orderId") long orderId) {

    	model.addAttribute("user",user);
    	if(user == null) {
    		return Result.error(CodeMsg.SESSION_ERROR);
    	}

    	OrderInfo order = orderService.getOrderById(orderId);
    	if(order == null) {
    		return Result.error(CodeMsg.ORDER_NOT_EXIST);
    	}


    	long goodsId = order.getGoodsId();
    	GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);


    	OrderDetailVo vo = new OrderDetailVo();
    	vo.setOrder(order);
    	vo.setGoods(goods);


    	return Result.success(vo);
    }
    
}

package com.chen.miaosha.controller;

import com.chen.miaosha.domain.MiaoShaUser;
import com.chen.miaosha.redis.GoodsKey;
import com.chen.miaosha.redis.RedisService;
import com.chen.miaosha.result.Result;
import com.chen.miaosha.service.GoodsService;
import com.chen.miaosha.vo.GoodsDetailVo;
import com.chen.miaosha.vo.GoodsVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.IWebContext;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;




import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 *  优化改进： 页面级缓存
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {

    @Autowired
    GoodsService goodsService;

    @Autowired
    RedisService redisService;


    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;


    /**
     *  展示商品列表
     *   优化： 页面级缓存
     *
     * @param model
     * @param user
     * @return
     */
    @RequestMapping(value = "/to_list",produces = "text/html")
    @ResponseBody
    public String list(HttpServletRequest request,HttpServletResponse response,Model model, MiaoShaUser user){
        model.addAttribute("user",user);

        // 先从 redis 缓存才能中查找数据
        String html = redisService.getKey(GoodsKey.getGoodsList, "", String.class);

        // 若缓存中有数据，则直接返回
        if(!StringUtils.isEmpty(html)){
            return html;
        }

        // 若缓存中没数据，则进入数据库中 查询商品详情
        List<GoodsVo> goodsList = goodsService.listGoodsVo();

        model.addAttribute("goodsList",goodsList);

        // 手动渲染页面
        IWebContext context = new WebContext(request,response,
                request.getServletContext(),request.getLocale(), model.asMap());

        html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);


        // 将从数据库中查询的数据 html 放入缓存redis中，以便下一次访问时使用
        if(!StringUtils.isEmpty(html)){
            redisService.setKey(GoodsKey.getGoodsList,"",html);
        }

        return html;
    }

    /**
     * 优化： 页面静态化  GoodsDetailVo
     *   展示商品详情:  返回一个 GoodsDetailVo
     * @param model
     * @param user
     * @param goodsId
     * @return
     */
    @RequestMapping(value = "/detail/{goodsId}")
    @ResponseBody
    public Result<GoodsDetailVo> goodsDetail(Model model, MiaoShaUser user, @PathVariable("goodsId")long goodsId){

        model.addAttribute("user",user);

        GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);


        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long now = System.currentTimeMillis();

        // 秒杀状态
        int miaoShaStatus = 0;
        // 秒杀剩余时间
        int remainSeconds = 0;

        // 秒杀还未开始，倒计时
        if(now < startTime){
            miaoShaStatus = 0;
            remainSeconds = (int) ((startTime - now )/1000);
        }else if(now > endTime){
            // 秒杀已结束
            miaoShaStatus = 2;
            remainSeconds = -1;
        }else { // 秒杀正在进行中
            miaoShaStatus = 1;
            remainSeconds = 0;
        }

        // 页面所需要的信息： GoodsDetailVo
        GoodsDetailVo vo = new GoodsDetailVo();
        vo.setGoods(goods);
        vo.setMiaoshaStatus(miaoShaStatus);
        vo.setRemainSeconds(remainSeconds);
        vo.setUser(user);

        return Result.success(vo);

    }


}

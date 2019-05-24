package com.chen.miaosha.service;

import com.chen.miaosha.dao.GoodsDao;
import com.chen.miaosha.domain.MiaoShaGoods;
import com.chen.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GoodsService {

    @Autowired
    GoodsDao goodsDao;

    public List<GoodsVo> listGoodsVo(){
        return goodsDao.listGoodsVo();
    }

    public GoodsVo getGoodsVoByGoodsId(long goodsId) {
        return goodsDao.getGoodsVoById(goodsId);
    }

    public boolean reduceStock(GoodsVo goods) {

        // 新建一个对象，避免修改数据时修改了不必要修改的属性，减少执行SQL语句的耗时
        MiaoShaGoods g = new MiaoShaGoods();
        g.setGoodsId(goods.getId());

        int ret = goodsDao.reduceStock(g);
        return ret > 0;
    }

    public void resetStock(List<GoodsVo> goodsList) {
        for(GoodsVo goods : goodsList ) {
            MiaoShaGoods g = new MiaoShaGoods();
            g.setGoodsId(goods.getId());
            g.setStockCount(goods.getStockCount());
            goodsDao.resetStock(g);
        }
    }

}

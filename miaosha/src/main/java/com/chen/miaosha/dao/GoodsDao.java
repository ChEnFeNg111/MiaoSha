package com.chen.miaosha.dao;

import com.chen.miaosha.domain.MiaoShaGoods;
import com.chen.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface GoodsDao {

    /**
     *  查询商品列表
     * @return
     */
    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id")
    List<GoodsVo>  listGoodsVo();

    /**
     *  根据 id 查询指定的商品
     * @param id
     * @return
     */
    @Select("select g.*,mg.stock_count, mg.start_date, mg.end_date,mg.miaosha_price from miaosha_goods mg left join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsVoById(@Param("goodsId") long id);

    /**
     *  库存减一:
     *    为了防止多并发时库存减成负数，增加了判断：
     *        1)  and stock_count > 0
     *        2)  建立数据库 miaosha_order 的唯一索引 UNIQUE( u_uid_gid : `user_id`, `goods_id`)
     *
     * @param g
     * @return
     */
    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
    int reduceStock(MiaoShaGoods g);

    /**
     *  更新库存
     * @param g
     * @return
     */
    @Update("update miaosha_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
    int resetStock(MiaoShaGoods g);
}

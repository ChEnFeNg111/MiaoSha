package com.chen.miaosha.domain;

import java.util.Date;

/**
 *  秒杀商品：
 *      private Long id					：秒杀商品id
 * 		private Long goodsId			: 对应商品表的商品id
 * 		private Integer stockCount		: 秒杀商品库存
 * 		private Date startDate			：秒杀开始时间
 * 		private Date endDate			：秒杀借结束时间
 */
public class MiaoShaGoods {
	private Long id;
	private Long goodsId;
	private Integer stockCount;
	private Date startDate;
	private Date endDate;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public Long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(Long goodsId) {
		this.goodsId = goodsId;
	}
	public Integer getStockCount() {
		return stockCount;
	}
	public void setStockCount(Integer stockCount) {
		this.stockCount = stockCount;
	}
	public Date getStartDate() {
		return startDate;
	}
	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}
	public Date getEndDate() {
		return endDate;
	}
	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}
}

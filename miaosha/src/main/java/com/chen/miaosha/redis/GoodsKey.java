package com.chen.miaosha.redis;

public class GoodsKey extends BasePrefix{

	private GoodsKey(int expireSeconds, String prefix) {
		super(expireSeconds, prefix);
	}

	// 页面的有效期为 60 s， 0：表示永久不失效
	public static GoodsKey getGoodsList = new GoodsKey(10, "gl");
	public static GoodsKey getGoodsDetail = new GoodsKey(10, "gd");
	public static GoodsKey getMiaoshaGoodsStock= new GoodsKey(0, "gs");
}

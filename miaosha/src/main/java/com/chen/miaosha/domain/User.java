package com.chen.miaosha.domain;

/**
 *  用户：
 *     private int id：     用户id
 * 	   private String name  用户姓名
 */
public class User {
	private int id;
	private String name;
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}

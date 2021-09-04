package com.qianyi.casinocore.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import io.swagger.annotations.ApiModel;
import lombok.Data;

@Entity
@Data
@ApiModel("用户绑定的银行卡")
public class BankcardsCustomer {

	@Column(unique = true)
	@Id
	private Integer id;

	/**
	 * 用户名
	 */
	private String account;
	
	/**
	 * 银行名
	 */
	private String bankName;

	/**
	 * 银行卡id
	 */
	private Integer bankId;

	/**
	 * 用户的银行/支付宝账号
	 */
	private String bankAccount;

	/**
	 * 省
	 */
	private String province;

	/**
	 * 市区
	 */
	private String city;

	/**
	 * 支行名,开户地址
	 */
	private String address;

	/**
	 * 开户名
	 */
	private String realName;

	/**
	 * 0:未禁用 1：禁用
	 */
	private Integer disable;

	/**
	 * 创建时间/绑定时间
	 */
	private Date createTime;

	/**
	 * 更新人
	 */
	private String updateBy;

	/**
	 * 更新时间
	 */
	private Date updateTime;

}

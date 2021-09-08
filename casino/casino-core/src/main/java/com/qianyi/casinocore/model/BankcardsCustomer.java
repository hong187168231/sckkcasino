package com.qianyi.casinocore.model;

import javax.persistence.Entity;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@Entity
@ApiModel("用户绑定的银行卡")
public class BankcardsCustomer extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 用户名
	 */
	@ApiModelProperty(value = "用户名")
	private String account;

	/**
	 * 银行名
	 */
	@ApiModelProperty(value = "银行名")
	private String bankName;

	/**
	 * 银行卡id
	 */
	@ApiModelProperty(value = "银行卡id")
	private Long bankId;

	/**
	 * 用户的银行/支付宝账号
	 */
	@ApiModelProperty(value = "用户的银行账号")
	private String bankAccount;

	/**
	 * 开户地址
	 */
	@ApiModelProperty(value = "开户地址")
	private String address;

	/**
	 * 开户名
	 */
	@ApiModelProperty(value = "开户名")
	private String realName;

	/**
	 * 0:未禁用 1：禁用
	 */
	@ApiModelProperty(value = "0:未禁用 1：禁用")
	private Integer disable;

	/**
	 * 默认卡，主卡
	 */
	@ApiModelProperty(value = "默认卡，主卡= 1")
	private Integer defaultCard;
	
	
	
//	/**
//	 *  针对绑定银行卡接口的参数合法性校验
//	 * @param bankName
//	 * @param bankId
//	 * @param bankAccount
//	 * @param address
//	 * @return
//	 */
//	public static String checkParamFroBound(String bankName, Long bankId, String bankAccount,
//			String address) {
//		if (StringUtils.isEmpty(bankName)) {
//			return "银行名不能为空！";
//		}
//		if (bankId == null) {
//			return "银行id不能为空！";
//		}
//		if (StringUtils.isEmpty(address)) {
//			return "开户地址不能为空！";
//		}
//		if (StringUtils.isEmpty(bankAccount)) {
//			return "银行账号不能为空！";
//		}
//		if (bankAccount.length() > 20 || bankAccount.length() < 16) {
//			return "长度只能在16~20位！";
//		}
//		return null;
//	}
	
}

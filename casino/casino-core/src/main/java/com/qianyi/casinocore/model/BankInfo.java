package com.qianyi.casinocore.model;

import javax.persistence.Entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("银行列表")
public class BankInfo extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 * 银行名
	 */
	private String bankName;

	/** 银行编码 */
	private String bankCode;

	private String bankLog;

	/** 银行类型：默认0:银行卡，1：支付宝，2：微信，3：QQ */
	private Integer bankType;

	/** 0:未禁用 1：禁用 */
	private Integer disable;

	/** 备注 */
	private String remark;

}

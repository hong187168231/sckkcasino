package com.qianyi.casinocore.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("银行列表")
public class BankInfo {

	@Id
	@Column(unique = true)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 银行名
	 */
	private String bankName;

	/** 银行编码 */
	private String bankCode;

	private String bankLog;

	/** 银行类型：默认0:银行卡，1：支付宝，2：微信，3：QQ */
	private Integer bankType;

	/** 创建时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	/** 创建人 */
	private String createBy;

	/** 0:未禁用 1：禁用 */
	private Integer disable;

	/** 备注 */
	private String remark;

}

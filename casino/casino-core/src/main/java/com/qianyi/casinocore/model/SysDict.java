package com.qianyi.casinocore.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("系统业务字典表")
public class SysDict {

	@Id
	@Column(unique = true)
	private Integer id;

	/**
	 * 业务编码
	 */
	private String code;
	
	/**
	 * 业务标志 code - flag 联合唯一索引
	 * 例如：
	 * code ： pay
	 * flag ：alipay
	 */
	private String flag;
	
	/** 名字 例如：支付宝 */
	private String name;

	/** 备注 */
	private String remark;

	/** 状态（1--正常 0--冻结） */
	private Integer status;

	/** 创建时间 */
	private Date createTime;

	/** 创建人 */
	private String createBy;

	/** 0:未禁用 1：禁用 */
	private Integer disable;

}

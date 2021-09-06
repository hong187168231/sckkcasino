package com.qianyi.casinocore.model;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author jordan
 */
@Entity
@Data
@Table(name = "sys_dict", uniqueConstraints = @UniqueConstraint(columnNames = {"type", "label"}))// 联合索引
@ApiModel("系统业务字典表")
public class SysDict {

	@Id
	@Column(unique = true)
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Integer id;

	/**
	 * 业务 type
	 */
	@Column(nullable = false, length = 32)
	private String type;
	
	/**
	 * 业务标志 label - label 联合唯一索引
	 * 例如：
	 * type ： pay
	 * label ：alipay
	 */
	@Column(length = 32)
	private String label;
	
	/** 值 具体的系统配置值 例如：type=pay：label:alipay value = 支付宝 */
	@Column(length = 32)
	private String value;
	
	/** 冗余字段*/
	private String value1;
	
	/** 冗余字段*/
	private String value2;
	
	/** 冗余字段*/
	private String value3;

	/** 备注 */
	private String remark;

	/** 状态（1--正常 0--冻结） */
	private Integer status;

	/** 创建时间 */
	@Temporal(TemporalType.TIMESTAMP)
	private Date createTime;

	/** 创建人 */
	@Column(length = 32)
	private String createBy;

}

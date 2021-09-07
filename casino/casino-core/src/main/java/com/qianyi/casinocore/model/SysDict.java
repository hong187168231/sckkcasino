package com.qianyi.casinocore.model;

import javax.persistence.Column;
import javax.persistence.Entity;

import io.swagger.annotations.ApiModel;
import lombok.Data;

/**
 * @author jordan
 */
@Data
@Entity
@ApiModel("系统业务字典表")
public class SysDict extends BaseEntity {

	private static final long serialVersionUID = 1L;

	/**
	 *	标签
	 */
	@Column(length = 32)
	private String label;
	
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
}

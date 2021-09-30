package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("打码明细表")
public class CodeNumChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "游戏记录ID")
	private Long gameRecordId;

	@ApiModelProperty(value = "打码量")
	private BigDecimal amount;

	@ApiModelProperty(value = "打码量变化前")
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "打码量变化后")
	private BigDecimal amountAfter;

}

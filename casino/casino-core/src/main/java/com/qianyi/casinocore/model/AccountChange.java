package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * @author jordan
 */
@Entity
@Data
@ApiModel("账变中心")
public class AccountChange extends BaseEntity {

	@ApiModelProperty(value = "用户ID")
	private Long userId;

	@ApiModelProperty(value = "订单号")
	private String orderNo;

	@ApiModelProperty(value = "账变类型")
	private Integer type;

	@ApiModelProperty(value = "额度变化")
	private BigDecimal amount;

	@ApiModelProperty(value = "额度变化前")
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "额度变化后")
	private BigDecimal amountAfter;

}

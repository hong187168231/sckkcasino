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

	@ApiModelProperty(value = "注单号")
	private String betId;

	@ApiModelProperty(value = "打码量")
	private BigDecimal amount;

	@ApiModelProperty(value = "打码量变化前")
	private BigDecimal amountBefore;

	@ApiModelProperty(value = "打码量变化后")
	private BigDecimal amountAfter;

	@ApiModelProperty(value = "清零值")
	private BigDecimal clearCodeNum;

	@ApiModelProperty(value = "0:消码，1:清0点")
	private Integer type;

	public static CodeNumChange setCodeNumChange(Long userId, GameRecord record, BigDecimal amount, BigDecimal amountBefore, BigDecimal amountAfter) {
		CodeNumChange codeNumChange = new CodeNumChange();
		codeNumChange.setUserId(userId);
		if (record != null) {
			codeNumChange.setGameRecordId(record.getId());
			codeNumChange.setBetId(record.getBetId());
		}
		codeNumChange.setAmount(amount);
		codeNumChange.setAmountBefore(amountBefore);
		codeNumChange.setAmountAfter(amountAfter);
		return codeNumChange;
	}
}

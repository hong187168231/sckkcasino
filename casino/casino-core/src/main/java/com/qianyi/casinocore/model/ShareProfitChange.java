package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
public class ShareProfitChange extends BaseEntity {
    @ApiModelProperty(value = "用户ID")
    private Long userId;

    @ApiModelProperty("会员账号")
    private String account;

    @ApiModelProperty(value = "订单号")
    private String orderNo;

    @ApiModelProperty(value = "账变类型 1:分润,2:充值")
    private Integer type;

    @ApiModelProperty(value = "额度变化")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal amount;

    @ApiModelProperty(value = "额度变化前")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal amountBefore;

    @ApiModelProperty(value = "额度变化后")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal amountAfter;

    @ApiModelProperty("返佣比例")
    private BigDecimal profitRate;

    @ApiModelProperty("贡献者")
    private Long fromUserId;

    @ApiModelProperty("返佣等级")
    private Integer parentLevel;

    @ApiModelProperty("有效投注金额")
    private BigDecimal validbet;

    public ShareProfitChange(Long userId, String account, Long fromUserId, BigDecimal amount, BigDecimal validbet) {
        this.userId = userId;
        this.account = account;
        this.fromUserId = fromUserId;
        this.amount = amount;
        this.validbet = validbet;
    }
}

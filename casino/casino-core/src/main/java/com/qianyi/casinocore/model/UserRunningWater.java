package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Entity
@Data
@ApiModel("会员每日流水表")
@Table(indexes = {@Index(name="identity_index",columnList = "userId",unique=true),@Index(name="identity_index",columnList = "staticsTimes",unique=true)})
public class UserRunningWater extends BaseEntity {
    @ApiModelProperty(value = "会员id")
    private Long userId;

    @ApiModelProperty(value = "当日流水金额")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal amount;

    @ApiModelProperty(value = "当日贡献佣金")
    @Column(columnDefinition = "Decimal(10,6) default '0.00'")
    private BigDecimal commission;

    @ApiModelProperty(value = "统计时间段")
    private String staticsTimes;
}

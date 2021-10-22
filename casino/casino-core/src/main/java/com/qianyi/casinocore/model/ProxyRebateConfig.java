package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("代理返佣等级配置")
public class ProxyRebateConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("第一级业绩额度")
    private Integer firstMoney;

    @ApiModelProperty("第一级返佣比例")
    private BigDecimal firstProfit;

    @ApiModelProperty("第二级业绩额度")
    private Integer secondMoney;

    @ApiModelProperty("第二级返佣比例")
    private BigDecimal secondProfit;

    @ApiModelProperty("第三级业绩额度")
    private Integer thirdMoney;

    @ApiModelProperty("第三级返佣比例")
    private BigDecimal thirdProfit;

    @ApiModelProperty("第四级业绩额度")
    private Integer fourMoney;

    @ApiModelProperty("第四级返佣比例")
    private BigDecimal fourProfit;

    @ApiModelProperty("第五级业绩额度")
    private Integer fiveMoney;

    @ApiModelProperty("第五级返佣比例")
    private BigDecimal fiveProfit;
}

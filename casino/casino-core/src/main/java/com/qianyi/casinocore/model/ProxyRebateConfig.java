package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

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
    private Double firstProfit;

    @ApiModelProperty("第二级业绩额度")
    private Integer secondMoney;

    @ApiModelProperty("第二级返佣比例")
    private Double secondProfit;

    @ApiModelProperty("第三级业绩额度")
    private Integer thirdMoney;

    @ApiModelProperty("第三级返佣比例")
    private Double thirdProfit;

    @ApiModelProperty("第四级业绩额度")
    private Integer fourMoney;

    @ApiModelProperty("第四级返佣比例")
    private Double fourProfit;

    @ApiModelProperty("第五级业绩额度")
    private Integer fiveMoney;

    @ApiModelProperty("第五级返佣比例")
    private Double fiveProfit;
}

package com.qianyi.casinocore.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
@Data
@Entity
@ApiModel("玩家推广返佣配置")
@JsonIgnoreProperties(ignoreUnknown = true)
public class PromoteCommissionConfig {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ApiModelProperty("一级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal firstCommission;

    @ApiModelProperty("二级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal secondCommission;

    @ApiModelProperty("三级玩家返佣")
    @Column(columnDefinition = "Decimal(10,6) default '0.000000'")
    private BigDecimal thirdCommission;

    /**
     * 游戏类型：1:WM,2:PG,3:CQ9
     */
    @ApiModelProperty("游戏类型：1:WM,2:PG,3:CQ9")
    private Integer gameType;
}

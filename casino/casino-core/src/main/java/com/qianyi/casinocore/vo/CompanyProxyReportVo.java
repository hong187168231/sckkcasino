package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CompanyProxyReportVo implements Serializable {
    private static final long serialVersionUID = -6875647823650305179L;
    @ApiModelProperty(value = "id")
    private Long id;
    @ApiModelProperty(value = "上级代理id")
    private Long parentId;
    @ApiModelProperty(value = "账号")
    private String userName;
    @ApiModelProperty(value = "昵称")
    private String nickName;
    @ApiModelProperty(value = "代理角色 1：总代理 2：区域代理 3：基层代理")
    private Integer proxyRole;
    @ApiModelProperty(value = "上级代理账号")
    private String superiorProxyAccount;
    @ApiModelProperty(value = "新增下级代理")
    private Integer groupNewProxyUsers;
    @ApiModelProperty(value = "团队新增玩家数")
    private Integer groupNewUsers;
    @ApiModelProperty(value = "活跃玩家数")
    private Integer activeUsers;
    @ApiModelProperty(value = "团队充值")
    private BigDecimal chargeAmount;
    @ApiModelProperty(value = "团队提款")
    private BigDecimal withdrawMoney;
    @ApiModelProperty("团队业绩(流水)")
    private BigDecimal groupPerformance;
    @ApiModelProperty("统计日期")
    private String staticsTimes;
}

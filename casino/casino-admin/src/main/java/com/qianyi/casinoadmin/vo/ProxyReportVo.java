package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class ProxyReportVo implements Serializable {

    private static final long serialVersionUID = -6875619842312305179L;
    @ApiModelProperty("id")
    private Long userId;
    @ApiModelProperty("账号")
    private String account;
    @ApiModelProperty("层级 0 当前 1 一级 2 二级 3 三级")
    private Integer tier;
    @ApiModelProperty("直属父级账号")
    private String firstPidAccount;
    @ApiModelProperty("直属父级ID")
    private Long firstPid;
    @ApiModelProperty("团队人数")
    private Integer allGroupNum;
    @ApiModelProperty("个人业绩流水")
    private BigDecimal performance;
    @ApiModelProperty("团队业绩流水")
    private BigDecimal allPerformance;
    @ApiModelProperty("贡献佣金")
    private BigDecimal contribution;
    @ApiModelProperty("统计时段")
    private String staticsTimes;
    @ApiModelProperty("返佣比例")
    private String commission;
}

package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CompanyVo implements Serializable {
    private static final long serialVersionUID = 2208529825210480975L;

    private Long id;

    private String companyName;

    private String createName;

    private String createDate;

    private Integer proxyNum;

    @ApiModelProperty(value = "抽点")
    private BigDecimal proxyOextract;
    @ApiModelProperty(value = "佣金")
    private BigDecimal proxyCommission;

}

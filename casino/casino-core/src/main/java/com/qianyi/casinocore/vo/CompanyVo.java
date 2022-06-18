package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Builder
@Data
public class CompanyVo implements Serializable {
    private static final long serialVersionUID = 2208529825210480975L;

    private Long id;

    private String companyName;

    private String createName;

    private String createDate;

    @Builder.Default
    private Integer proxyNum = 0;

    @ApiModelProperty(value = "抽点")
    @Builder.Default
    private BigDecimal proxyOextract = BigDecimal.ZERO;

    @ApiModelProperty(value = "佣金")
    @Builder.Default
    private BigDecimal proxyCommission = BigDecimal.ZERO;

}

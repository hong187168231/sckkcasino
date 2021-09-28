package com.qianyi.casinoadmin.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;

@Data
public class BetRatioConfigVo {

    @ApiModelProperty("名称")
    private String name;

    @ApiModelProperty("打码倍率")
    private Float codeTimes;

    @ApiModelProperty("最低金额重置打码量")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal minMoney;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import java.math.BigDecimal;

@Data
@Entity
public class ProxyDayReport extends BaseEntity{

    private Long userId;

    @ApiModelProperty("有效投注")
    private BigDecimal betAmount = BigDecimal.ZERO;
    @ApiModelProperty("分润")
    private BigDecimal profitAmount= BigDecimal.ZERO;
    @ApiModelProperty("今日新增")
    private Integer newNum;
    @ApiModelProperty("团队人数")
    private Integer groupNum;
    @ApiModelProperty("直属下级")
    private Integer directNum;
    @ApiModelProperty("日期")
    private String dayTime;

}

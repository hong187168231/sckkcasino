package com.qianyi.casinoweb.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

/**
 * 代理中心佣金
 */
@Data
@ApiModel("代理中心")
public class ProxyCentreVo {

    @ApiModelProperty(value = "今日佣金")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal todayCommission;

    @ApiModelProperty(value = "昨日佣金")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal yesterdayCommission;

    @ApiModelProperty(value = "本周佣金")
    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private BigDecimal weekCommission;

    @Data
    @ApiModel("我的团队")
    public static class MyTeam{

        @ApiModelProperty(value = "今日存款")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal deppositeAmount;

        @ApiModelProperty(value = "今日有效投注")
        @JsonFormat(shape = JsonFormat.Shape.STRING)
        private BigDecimal betAmount;

        @ApiModelProperty("今日新增")
        private Integer newNum;

        @ApiModelProperty("团队总人数")
        private Integer allGroupNum;

        @ApiModelProperty("直属人数")
        private Integer directGroupNum;
    }
}

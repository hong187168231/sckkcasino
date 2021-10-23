package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class CompanyOrderAmountVo {

    @ApiModelProperty("总代ID")
    private Long firstProxy;

    @ApiModelProperty("区域代理ID")
    private Long secondProxy;

    @ApiModelProperty("基层代理ID")
    private Long thirdProxy;

    @ApiModelProperty("玩家数")
    private Integer playerNum;

    /**
     * 下注時間
     */
    @ApiModelProperty(value = "下注時間")
    private String betTime;


    /**
     * 有效下注
     */
    @ApiModelProperty(value = "有效下注")
    private String validbet;
}

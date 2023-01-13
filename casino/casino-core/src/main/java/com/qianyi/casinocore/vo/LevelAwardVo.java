package com.qianyi.casinocore.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.qianyi.modulecommon.config.Decimal2Serializer;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class LevelAwardVo {

    private Long userId;

    private Long firstProxy;

    private Long secondProxy;

    private Long thirdProxy;

    private BigDecimal todayAward;

    private BigDecimal riseAward;

    private BigDecimal betAmount;


}
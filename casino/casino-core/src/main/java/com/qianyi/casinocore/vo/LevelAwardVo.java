package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
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

    private BigDecimal winLoss;


}
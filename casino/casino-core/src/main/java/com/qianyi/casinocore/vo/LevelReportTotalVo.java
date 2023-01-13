package com.qianyi.casinocore.vo;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.math.BigDecimal;

@Data
@ApiModel
public class LevelReportTotalVo {

    private BigDecimal validBet;

    private BigDecimal winLoss;

    private BigDecimal todayAward;

    private BigDecimal riseAward;

}
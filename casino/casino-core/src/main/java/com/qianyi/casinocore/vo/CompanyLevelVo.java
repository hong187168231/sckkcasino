package com.qianyi.casinocore.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompanyLevelVo {

    //返佣等级
    private String profitLevel;

    //返佣金额（每"返佣金额线"返多少）
    private BigDecimal profitAmount;
    //返佣金额线
    private BigDecimal profitAmountLine;
}

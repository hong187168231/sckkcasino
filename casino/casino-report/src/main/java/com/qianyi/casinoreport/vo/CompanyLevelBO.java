package com.qianyi.casinoreport.vo;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CompanyLevelBO {

    //返佣等级
    private Integer profitLevel;
    //实际倍数
    private Integer profitActTimes;
    //返佣金额（每1w返多少）
    private BigDecimal profitAmount;
}

package com.qianyi.casinoadmin.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
public class ThridBalanceSumVo implements Serializable {
    private static final long serialVersionUID = -3881851635675684013L;

    private BigDecimal sunBalance;

    private String queryTime;
}

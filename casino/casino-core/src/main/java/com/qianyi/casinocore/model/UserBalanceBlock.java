package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;

/**
 * 用户资金冻结表
 */
@Entity
@Data
public class UserBalanceBlock extends BaseEntity {

    private String userName;

    private String userId;

    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal money;

    /**
     * 状态 1：解冻  2：冻结
     */
    private Integer status;
}

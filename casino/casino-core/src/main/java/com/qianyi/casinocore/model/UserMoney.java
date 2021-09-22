package com.qianyi.casinocore.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.math.BigDecimal;

@Data
@Entity
@Table(name ="user_money",uniqueConstraints={@UniqueConstraint(columnNames={"userId"})})
public class UserMoney extends BaseEntity{

    private Long userId;

    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal money;

    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal codeNum;

    //计算可提现金额
    public BigDecimal getWithdrawMoney(){
        BigDecimal defaultVal = BigDecimal.ZERO.setScale(2);
        //打码量为0时才有可提现金额
        if (this.codeNum != null && BigDecimal.ZERO.compareTo(this.codeNum) == 0) {
            BigDecimal money = this.money == null ? defaultVal : this.money;
            return money.setScale(2, BigDecimal.ROUND_HALF_UP);
        }
        return defaultVal;
    }
}

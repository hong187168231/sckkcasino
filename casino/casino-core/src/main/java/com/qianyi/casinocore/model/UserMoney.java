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
}

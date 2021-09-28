package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@ApiModel("ip黑名单")
@Table(name ="ip_black",uniqueConstraints={@UniqueConstraint(columnNames={"ip"})})
public class IpBlack extends BaseEntity{

    private String ip;

    /** 0:未禁用 1：禁用 */
    private Integer status;
}
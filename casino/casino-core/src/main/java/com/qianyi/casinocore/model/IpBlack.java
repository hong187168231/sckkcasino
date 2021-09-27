package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("ip黑名单")
public class IpBlack extends BaseEntity{

    private String ip;

    /** 0:未禁用 1：禁用 */
    private Integer status;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;

@Data
@Entity
@ApiModel("ip白名单")
@Table(indexes = {@Index(name="identity_index",columnList = "ip",unique=true),@Index(name="identity_index",columnList = "type",unique=true)})
public class IpWhite extends BaseEntity {

    @ApiModelProperty(value = "ip")
    private String ip;

    /** 1:总控管理员 */
    @ApiModelProperty(value = "1:总控管理员")
    private Integer type;

    @ApiModelProperty(value = "备注")
    private String remark;
}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Index;
import javax.persistence.Table;
import java.math.BigDecimal;

@Data
@Entity
@ApiModel("用户洗码配置表")
@Table(indexes = {@Index(name="identity_index",columnList = "userId",unique=true),@Index(name="identity_index",columnList = "platform",unique=true)
    ,@Index(name="identity_index",columnList = "gameId",unique=true)})
public class UserWashCodeConfig extends BaseEntity{

    @ApiModelProperty(value = "用户id")
    private Long userId;

    @ApiModelProperty(value = "平台")
    private String platform;

    @ApiModelProperty(value = "游戏ID")
    private String gameId;

    @ApiModelProperty(value = "游戏名称")
    private String gameName;

    @ApiModelProperty(value = "游戏英文名称")
    private String gameEnName;

    @ApiModelProperty(value = "返水比例")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal rate = BigDecimal.ZERO;

    @ApiModelProperty(value = "状态：0:禁用，1:启用")
    private Integer state;
    @ApiModelProperty(value = "每日最低投注量")
    @Column(columnDefinition = "Decimal(10,2) default '0.00'")
    private BigDecimal minBet = BigDecimal.ZERO;


}

package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

@Data
@Entity
@ApiModel("VNC游戏记录抓取时间记录表")
public class GameRecordVNCEndTime extends BaseEntity{

    @ApiModelProperty(value = "数据拉取开始时间 UTC-7时间")
    private String startTime;

    @ApiModelProperty(value = "数据拉取结束时间 UTC-7时间")
    private String endTime;

    @ApiModelProperty(value = "游戏平台名称")
    private String platform;

    @ApiModelProperty(value = "状态:0.失败，1.成功")
    private Integer status;

}

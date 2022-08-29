package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("AE游戏记录抓取时间记录表")
public class GameRecordAeEndTime extends BaseEntity{

    @ApiModelProperty(value = "数据拉取开始时间 ISO 8601 格式")
    private String startTime;

    @ApiModelProperty(value = "数据拉取结束时间 ISO 8601 格式")
    private String endTime;

    @ApiModelProperty(value = "游戏平台名称")
    private String platform;

    @ApiModelProperty(value = "状态:0.失败，1.成功")
    private Integer status;


}

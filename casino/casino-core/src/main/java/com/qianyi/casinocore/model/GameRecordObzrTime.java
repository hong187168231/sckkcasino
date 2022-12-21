package com.qianyi.casinocore.model;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.persistence.Entity;

@Data
@Entity
@ApiModel("OB游戏记录抓取时间记录表")
public class GameRecordObzrTime extends BaseEntity{

    @ApiModelProperty(value = "数据拉取结束时间")
    private String endTime;

}

package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqBetRecordHistoryDTO", description = "下注记录查询参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqBetRecordHistoryDTO extends BaseReqModel {

//    @ApiModelProperty(value = "游戏账号，可选", required = false)
//    private String loginName;


////////////////////////历史拉单
    @ApiModelProperty(value = "开始时间，格式yyyy-MM-dd HH:mm:ss，区间最大1小时", required = true)
    private String startTime;
    @ApiModelProperty(value = "结束时间，格式yyyy-MM-dd HH:mm:ss，区间最大1小时", required = true)
    private String endTime;
    @ApiModelProperty(value = "查询页码", required = true)
    private Integer pageIndex;


}

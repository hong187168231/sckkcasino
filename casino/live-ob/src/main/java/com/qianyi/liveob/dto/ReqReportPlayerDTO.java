package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 *
 */
@ApiModel(value = "ReqReportPlayerDTO", description = "会员输赢报表查询参数模型")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqReportPlayerDTO extends BaseReqModel {

    @ApiModelProperty(value = "开始日期", required = true)
    private Integer startDate;
    @ApiModelProperty(value = "结束日期", required = true)
    private Integer endDate;
    @ApiModelProperty(value = "查询页码", required = true)
    private Integer pageIndex;


}

package com.qianyi.casinoadmin.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;


@Data
@ApiModel
public class VipReportOtherProxyDTO {

    @ApiModelProperty(value = "起始时间查询")
    private String startTime;
    @ApiModelProperty(value = "结束时间查询")
    private String endTime;
    @ApiModelProperty(hidden = true,value = "代理id")
    private Long proxyUserId;

}

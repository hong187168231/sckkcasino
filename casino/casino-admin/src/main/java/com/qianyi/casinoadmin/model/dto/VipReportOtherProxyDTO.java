package com.qianyi.casinoadmin.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;


@Data
@ApiModel
public class VipReportOtherProxyDTO {

    @ApiModelProperty(value = "起始时间查询")
    private String startTime;
    @ApiModelProperty(value = "结束时间查询")
    private String endTime;

    @ApiModelProperty(value = "起始时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startDate;
    @ApiModelProperty(value = "结束时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endDate;


    @ApiModelProperty(value = "当前代理账号")
    private String proxyUserName;

    @ApiModelProperty(hidden = true,value = "总代id")
    private Long firstProxyId;

    @ApiModelProperty(hidden = true,value = "区代id")
    private Long secondProxyId;




}

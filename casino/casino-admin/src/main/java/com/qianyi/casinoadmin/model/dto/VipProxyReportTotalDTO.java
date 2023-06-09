package com.qianyi.casinoadmin.model.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.Transient;


@Data
@ApiModel
public class VipProxyReportTotalDTO {

    @ApiModelProperty(value = "账号")
    private String proxyUserName;
    @ApiModelProperty(value = "起始时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startTime;
    @ApiModelProperty(value = "结束时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endTime;

    @ApiModelProperty(value = "起始时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String startDate;
    @ApiModelProperty(value = "结束时间查询")
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private String endDate;


    @ApiModelProperty(hidden = true,value = "代理账号Id")
    private Long proxyUserId;
    @ApiModelProperty(hidden = true,value = "代理权限标识")
    private Integer proxyLevel;


    private  String queryZd;

}

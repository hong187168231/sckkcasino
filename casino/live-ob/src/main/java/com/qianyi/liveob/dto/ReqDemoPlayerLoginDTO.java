package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 试玩账号登录
 */
@ApiModel(value = "ReqDemoPlayerLoginDTO", description = "试玩账号登录")
@EqualsAndHashCode(callSuper = true)
@Data
public class ReqDemoPlayerLoginDTO extends BaseReqModel {

    @ApiModelProperty(value = "设备类型", required = true)
    private Integer deviceType;
    @ApiModelProperty(value = "语言", required = true)
    private Integer lang;
    @ApiModelProperty(value = "异常情况时返回商户地址.H5端有用", required = true)
    private String backurl;
}

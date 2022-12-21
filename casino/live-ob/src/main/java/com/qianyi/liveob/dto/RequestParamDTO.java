package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
@ApiModel(value = "RequestParamDTO", description = "接口请求参数")
public class RequestParamDTO {

    @ApiModelProperty(value = "商户号", required = true)
    private String merchantCode;

    @ApiModelProperty(value = "参数密文", required = true)
    private String params;

    @ApiModelProperty(value = "参数签名", required = true)
    private String signature;
}

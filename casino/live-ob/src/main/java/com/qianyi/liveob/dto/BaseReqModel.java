package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 *
 *
 * @author albert
 * @since 2/14/20 4:29 PM
 * @version 0.1
 */
@ApiModel(value = "BaseReqModel", description = "基础模型")
@Data
public class BaseReqModel {
    @ApiModelProperty(value = "13位数时间戳", required = true)
    private Long timestamp;
}

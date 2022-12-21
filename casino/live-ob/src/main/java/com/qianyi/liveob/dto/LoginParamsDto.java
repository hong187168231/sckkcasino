package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;

/**
 * @author joy
 * @date 2020/2/17 22:24
 */
@Data
@Builder
public class LoginParamsDto {

    @ApiModelProperty(value = "玩家ID", required = true)
    private Long playerId;

    @ApiModelProperty(value = "设备类型", required = true)
    private Integer deviceType;

    @ApiModelProperty(value = "语言，商户也可能不传递", required = false)
    private Integer lang;

    @ApiModelProperty(value = "限红，试玩时无参数，商户也可能不传递", required = false)
    private Integer oddType;


}

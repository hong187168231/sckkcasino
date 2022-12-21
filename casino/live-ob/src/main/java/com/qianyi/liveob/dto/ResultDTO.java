package com.qianyi.liveob.dto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author joy
 * @date 2019/9/12 17:30
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResultDTO {

    @ApiModelProperty("余额")
    private String code;

    @ApiModelProperty("余额")
    private PageRespDTO data;

    @ApiModelProperty("余额")
    private String message;
}
